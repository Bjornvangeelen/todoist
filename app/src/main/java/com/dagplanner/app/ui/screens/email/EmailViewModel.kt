package com.dagplanner.app.ui.screens.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagplanner.app.data.model.EmailMessage
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.repository.GmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailUiState(
    val emails: List<EmailMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val accountName: String? = null,
    val selectedEmail: EmailMessage? = null,
    val isLoadingBody: Boolean = false,
)

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val repository: GmailRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.googleAccountName.collect { account ->
                _uiState.value = _uiState.value.copy(accountName = account)
                if (account != null && _uiState.value.emails.isEmpty()) {
                    loadInbox()
                }
            }
        }
    }

    fun loadInbox() {
        val account = _uiState.value.accountName ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.fetchInbox(account)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                emails = result.getOrDefault(emptyList()),
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    fun openEmail(email: EmailMessage) {
        _uiState.value = _uiState.value.copy(selectedEmail = email)
        if (email.body == null) {
            loadBody(email)
        }
    }

    fun closeEmail() {
        _uiState.value = _uiState.value.copy(selectedEmail = null, isLoadingBody = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadBody(email: EmailMessage) {
        val account = _uiState.value.accountName ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingBody = true)
            val result = repository.fetchBody(account, email.id)
            val bodyText = result.getOrNull() ?: ""
            val updated = email.copy(body = bodyText)
            _uiState.value = _uiState.value.copy(
                isLoadingBody = false,
                selectedEmail = updated,
                emails = _uiState.value.emails.map { if (it.id == email.id) updated else it },
            )
        }
    }
}
