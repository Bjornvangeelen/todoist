package com.dagplanner.app.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskType
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.FirestoreShoppingRepository
import com.dagplanner.app.data.repository.TaskRepository
import com.dagplanner.app.ui.screens.tasks.TaskEditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val firestoreRepository: FirestoreShoppingRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val householdId: StateFlow<String?> = userPreferences.householdId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<Task>> = userPreferences.householdId
        .flatMapLatest { householdId ->
            if (householdId != null) {
                firestoreRepository.getItems(householdId) { error ->
                    _error.value = "Laden mislukt: ${error.localizedMessage}"
                }
            } else {
                repository.getTasksByType(TaskType.SHOPPING)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow(TaskEditorState())
    val editorState: StateFlow<TaskEditorState> = _editorState.asStateFlow()

    fun openNewItemEditor() {
        _editorState.value = TaskEditorState(isOpen = true, taskType = TaskType.SHOPPING)
    }

    fun openEditItemEditor(item: Task) {
        _editorState.value = TaskEditorState(
            isOpen = true,
            taskToEdit = item,
            taskType = TaskType.SHOPPING,
            title = item.title,
            date = item.date,
            time = item.time,
            label = item.label ?: "",
            priority = item.priority,
            deadline = item.deadline,
            location = item.location ?: "",
            reminder = item.reminder,
        )
    }

    fun closeEditor() {
        _editorState.value = TaskEditorState()
    }

    fun clearError() {
        _error.value = null
    }

    fun updateField(update: TaskEditorState.() -> TaskEditorState) {
        _editorState.value = _editorState.value.update()
    }

    fun saveItem() {
        val s = _editorState.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            _editorState.value = s.copy(isSaving = true)
            val item = (s.taskToEdit ?: Task(title = "", taskType = TaskType.SHOPPING)).copy(
                title = s.title.trim(),
                taskType = TaskType.SHOPPING,
                date = s.date,
                time = s.time,
                label = s.label.ifBlank { null },
                priority = s.priority,
                deadline = s.deadline,
                location = s.location.ifBlank { null },
                reminder = s.reminder,
            )
            try {
                val hid = householdId.value
                if (hid != null) {
                    firestoreRepository.upsertItem(hid, item)
                } else {
                    repository.upsertTask(item)
                }
                closeEditor()
            } catch (e: Exception) {
                _editorState.value = s.copy(isSaving = false)
                _error.value = "Opslaan mislukt: ${e.localizedMessage}"
            }
        }
    }

    fun deleteItem(item: Task) {
        viewModelScope.launch {
            try {
                val hid = householdId.value
                if (hid != null) {
                    firestoreRepository.deleteItem(hid, item)
                } else {
                    repository.deleteTask(item)
                }
            } catch (e: Exception) {
                _error.value = "Verwijderen mislukt: ${e.localizedMessage}"
            }
            closeEditor()
        }
    }

    fun toggleComplete(item: Task) {
        viewModelScope.launch {
            try {
                val toggled = item.copy(isCompleted = !item.isCompleted)
                val hid = householdId.value
                if (hid != null) {
                    firestoreRepository.upsertItem(hid, toggled)
                } else {
                    repository.toggleComplete(item)
                }
            } catch (e: Exception) {
                _error.value = "Bijwerken mislukt: ${e.localizedMessage}"
            }
        }
    }
}
