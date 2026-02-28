package com.dagplanner.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskPriority
import com.dagplanner.app.data.model.TaskType
import com.dagplanner.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TaskEditorState(
    val isOpen: Boolean = false,
    val taskToEdit: Task? = null,
    val taskType: TaskType = TaskType.TASK,
    val title: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val label: String = "",
    val priority: TaskPriority = TaskPriority.NONE,
    val deadline: LocalDate? = null,
    val location: String = "",
    val reminder: String? = null,
    val isSaving: Boolean = false,
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getTasksByType(TaskType.TASK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow(TaskEditorState())
    val editorState: StateFlow<TaskEditorState> = _editorState.asStateFlow()

    fun openNewTaskEditor() {
        _editorState.value = TaskEditorState(isOpen = true, taskType = TaskType.TASK)
    }

    fun openEditTaskEditor(task: Task) {
        _editorState.value = TaskEditorState(
            isOpen = true,
            taskToEdit = task,
            taskType = task.taskType,
            title = task.title,
            date = task.date,
            time = task.time,
            label = task.label ?: "",
            priority = task.priority,
            deadline = task.deadline,
            location = task.location ?: "",
            reminder = task.reminder,
        )
    }

    fun closeEditor() {
        _editorState.value = TaskEditorState()
    }

    fun updateField(update: TaskEditorState.() -> TaskEditorState) {
        _editorState.value = _editorState.value.update()
    }

    fun saveTask() {
        val s = _editorState.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            _editorState.value = s.copy(isSaving = true)
            val task = (s.taskToEdit ?: Task(title = "", taskType = s.taskType)).copy(
                title = s.title.trim(),
                taskType = s.taskType,
                date = s.date,
                time = s.time,
                label = s.label.ifBlank { null },
                priority = s.priority,
                deadline = s.deadline,
                location = s.location.ifBlank { null },
                reminder = s.reminder,
            )
            repository.upsertTask(task)
            closeEditor()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            closeEditor()
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch { repository.toggleComplete(task) }
    }
}
