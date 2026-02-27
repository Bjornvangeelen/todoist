package com.dagplanner.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.CalendarEvent
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

data class EventEditorState(
    val isOpen: Boolean = false,
    val eventToEdit: CalendarEvent? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val isAllDay: Boolean = false,
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endDate: LocalDate = LocalDate.now(),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val isSaving: Boolean = false,
)

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val googleAccountName: String? = null,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow(EventEditorState())
    val editorState: StateFlow<EventEditorState> = _editorState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    /** Evenementen voor de geselecteerde dag */
    val selectedDayEvents: StateFlow<List<CalendarEvent>> = _selectedDate
        .flatMapLatest { date ->
            repository.getEventsForDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Evenementen voor de weergegeven maand (voor maandoverzicht met stipjes) */
    val monthEvents: StateFlow<List<CalendarEvent>> = _uiState
        .flatMapLatest { state ->
            val month = state.displayedMonth
            repository.getEventsInRange(
                from = month.atDay(1),
                to = month.atEndOfMonth()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            userPreferences.googleAccountName.collect { account ->
                _uiState.value = _uiState.value.copy(googleAccountName = account)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            displayedMonth = YearMonth.from(date)
        )
    }

    fun navigateMonth(direction: Int) {
        val newMonth = _uiState.value.displayedMonth.plusMonths(direction.toLong())
        _uiState.value = _uiState.value.copy(displayedMonth = newMonth)
    }

    fun syncCalendar() {
        val accountName = _uiState.value.googleAccountName ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null)
            val result = repository.syncGoogleCalendar(accountName)
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun openNewEventEditor(date: LocalDate = _uiState.value.selectedDate) {
        _editorState.value = EventEditorState(
            isOpen = true,
            startDate = date,
            endDate = date,
        )
    }

    fun openEditEventEditor(event: CalendarEvent) {
        _editorState.value = EventEditorState(
            isOpen = true,
            eventToEdit = event,
            title = event.title,
            description = event.description ?: "",
            location = event.location ?: "",
            isAllDay = event.isAllDay,
            startDate = event.startDate,
            startTime = event.startTime ?: LocalTime.of(9, 0),
            endDate = event.endDate,
            endTime = event.endTime ?: LocalTime.of(10, 0),
        )
    }

    fun closeEventEditor() {
        _editorState.value = EventEditorState()
    }

    fun updateEditorField(update: EventEditorState.() -> EventEditorState) {
        _editorState.value = _editorState.value.update()
    }

    fun saveEvent() {
        val account = _uiState.value.googleAccountName ?: return
        val s = _editorState.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _editorState.value = s.copy(isSaving = true)
            val result = if (s.eventToEdit == null) {
                repository.createEvent(
                    accountName = account,
                    title = s.title.trim(),
                    description = s.description.ifBlank { null },
                    location = s.location.ifBlank { null },
                    isAllDay = s.isAllDay,
                    startDate = s.startDate,
                    startTime = if (s.isAllDay) null else s.startTime,
                    endDate = s.endDate,
                    endTime = if (s.isAllDay) null else s.endTime,
                )
            } else {
                repository.updateEvent(
                    accountName = account,
                    event = s.eventToEdit,
                    title = s.title.trim(),
                    description = s.description.ifBlank { null },
                    location = s.location.ifBlank { null },
                    isAllDay = s.isAllDay,
                    startDate = s.startDate,
                    startTime = if (s.isAllDay) null else s.startTime,
                    endDate = s.endDate,
                    endTime = if (s.isAllDay) null else s.endTime,
                )
            }
            if (result.isSuccess) {
                closeEventEditor()
            } else {
                _editorState.value = s.copy(isSaving = false)
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        val account = _uiState.value.googleAccountName ?: return
        viewModelScope.launch {
            _editorState.value = _editorState.value.copy(isSaving = true)
            val result = repository.deleteEvent(account, event)
            if (result.isSuccess) {
                closeEventEditor()
            } else {
                _editorState.value = _editorState.value.copy(isSaving = false)
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }
}
