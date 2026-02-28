package com.dagplanner.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.CalendarRepository
import com.dagplanner.app.data.repository.FirestoreShoppingRepository
import com.dagplanner.app.data.repository.TaskRepository
import com.dagplanner.app.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val googleAccountName: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val selectedTheme: AppTheme = AppTheme.OCEAAN_BLAUW,
    val householdId: String? = null,
    val isHouseholdLoading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val calendarRepository: CalendarRepository,
    private val firestoreShoppingRepository: FirestoreShoppingRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.googleAccountName.collect { account ->
                _uiState.value = _uiState.value.copy(googleAccountName = account)
            }
        }
        viewModelScope.launch {
            userPreferences.selectedTheme.collect { theme ->
                _uiState.value = _uiState.value.copy(selectedTheme = theme)
            }
        }
        viewModelScope.launch {
            userPreferences.householdId.collect { id ->
                _uiState.value = _uiState.value.copy(householdId = id)
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferences.setTheme(theme)
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

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null, error = null)
    }

    fun createHousehold() {
        val code = generateCode()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHouseholdLoading = true, error = null)
            try {
                firestoreShoppingRepository.createHousehold(code)
                val localItems = taskRepository.getShoppingItemsOnce()
                localItems.forEach { item ->
                    firestoreShoppingRepository.upsertItem(code, item)
                }
                userPreferences.setHouseholdId(code)
                _uiState.value = _uiState.value.copy(
                    isHouseholdLoading = false,
                    syncMessage = "Huishouden aangemaakt met code: $code"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isHouseholdLoading = false,
                    error = "Aanmaken mislukt: ${e.localizedMessage}"
                )
            }
        }
    }

    fun joinHousehold(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Code moet 6 tekens lang zijn")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHouseholdLoading = true, error = null)
            try {
                val exists = firestoreShoppingRepository.householdExists(trimmed)
                if (exists) {
                    userPreferences.setHouseholdId(trimmed)
                    _uiState.value = _uiState.value.copy(
                        isHouseholdLoading = false,
                        syncMessage = "Succesvol gekoppeld met huishouden $trimmed"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isHouseholdLoading = false,
                        error = "Huishouden '$trimmed' niet gevonden. Controleer de code."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isHouseholdLoading = false,
                    error = "Koppelen mislukt: ${e.localizedMessage}"
                )
            }
        }
    }

    fun leaveHousehold() {
        viewModelScope.launch {
            userPreferences.clearHouseholdId()
        }
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
