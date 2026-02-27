package com.dagplanner.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val googleAccountName: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.googleAccountName.collect { account ->
                _uiState.value = _uiState.value.copy(googleAccountName = account)
            }
        }
    }

    fun onGoogleAccountLinked(accountName: String) {
        viewModelScope.launch {
            userPreferences.setGoogleAccountName(accountName)
            _uiState.value = _uiState.value.copy(
                googleAccountName = accountName,
                syncMessage = null,
                error = null
            )
            // Direct synchroniseren na koppeling
            syncNow(accountName)
        }
    }

    fun unlinkGoogleAccount() {
        viewModelScope.launch {
            userPreferences.clearGoogleAccount()
            _uiState.value = _uiState.value.copy(
                googleAccountName = null,
                syncMessage = null
            )
        }
    }

    fun syncNow(accountName: String? = null) {
        val account = accountName ?: _uiState.value.googleAccountName ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null)
            val result = calendarRepository.syncGoogleCalendar(account)
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncMessage = result.fold(
                    onSuccess = { count -> "$count evenementen gesynchroniseerd" },
                    onFailure = { null }
                ),
                error = result.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null, error = null)
    }
}
