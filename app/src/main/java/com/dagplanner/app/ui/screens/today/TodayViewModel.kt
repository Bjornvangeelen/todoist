package com.dagplanner.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.CalendarEvent
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskType
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.CalendarRepository
import com.dagplanner.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val tasks: List<Task> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val accountName: String? = null,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<TodayUiState> = combine(
        taskRepository.getTasksForDate(TaskType.TASK, today),
        calendarRepository.getEventsForDate(today),
        userPreferences.googleAccountName,
    ) { tasks, events, account ->
        TodayUiState(
            tasks = tasks,
            events = events.sortedBy { it.startTime },
            accountName = account,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())
}
