package com.dagplanner.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskPriority
import com.dagplanner.app.data.model.TaskRecurrence
import com.dagplanner.app.data.model.TaskType
import com.dagplanner.app.data.repository.TaskRepository
import com.dagplanner.app.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

enum class TaskSortOrder(val label: String) {
    DEFAULT("Standaard"),
    DATE("Datum"),
    PRIORITY("Prioriteit"),
    TITLE("Naam"),
}

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
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val isSaving: Boolean = false,
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(TaskSortOrder.DEFAULT)
    val sortOrder: StateFlow<TaskSortOrder> = _sortOrder.asStateFlow()

    private val allTasks: StateFlow<List<Task>> = repository.getTasksByType(TaskType.TASK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = combine(allTasks, _searchQuery, _sortOrder) { tasks, query, sort ->
        val filtered = if (query.isBlank()) tasks
        else tasks.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.label?.contains(query, ignoreCase = true) == true
        }
        when (sort) {
            TaskSortOrder.DEFAULT  -> filtered
            TaskSortOrder.DATE     -> filtered.sortedWith(compareBy(nullsLast()) { it.date })
            TaskSortOrder.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
            TaskSortOrder.TITLE    -> filtered.sortedBy { it.title.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow(TaskEditorState())
    val editorState: StateFlow<TaskEditorState> = _editorState.asStateFlow()

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOrder(order: TaskSortOrder) { _sortOrder.value = order }

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
            recurrence = task.recurrence,
        )
    }

    fun closeEditor() { _editorState.value = TaskEditorState() }

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
                recurrence = s.recurrence,
            )
            repository.upsertTask(task)
            reminderScheduler.schedule(task)
            closeEditor()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            reminderScheduler.cancel(task.id)
            repository.deleteTask(task)
            closeEditor()
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            repository.toggleComplete(task)
            // Maak volgende herhalende taak aan als dit de eerste keer is dat de taak wordt afgerond
            if (!task.isCompleted && task.recurrence != TaskRecurrence.NONE && task.date != null) {
                val nextDate = when (task.recurrence) {
                    TaskRecurrence.DAILY   -> task.date.plusDays(1)
                    TaskRecurrence.WEEKLY  -> task.date.plusWeeks(1)
                    TaskRecurrence.MONTHLY -> task.date.plusMonths(1)
                    TaskRecurrence.NONE    -> null
                }
                if (nextDate != null) {
                    val nextTask = task.copy(
                        id = UUID.randomUUID().toString(),
                        date = nextDate,
                        isCompleted = false,
                        createdAt = System.currentTimeMillis(),
                    )
                    repository.upsertTask(nextTask)
                    reminderScheduler.schedule(nextTask)
                }
            }
        }
    }
}
