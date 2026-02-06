package com.ijreddy.loanapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    // TODO: Inject AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isScopedCustomer = MutableStateFlow(false)
    val isScopedCustomer: StateFlow<Boolean> = _isScopedCustomer.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // TODO: Check stored session via Supabase
            // For now, start unauthenticated
            _isAuthenticated.value = false
        }
    }

    fun login(emailOrPhone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Normalize phone to email format (matching web app pattern)
                val email = normalizeIdentifier(emailOrPhone)

                // TODO: Implement actual Supabase auth
                // val response = supabase.auth.signInWith(Email) {
                //     this.email = email
                //     this.password = password
                // }

                // Temporary: Simulate success for structure testing
                // Remove this when implementing real auth
                _isAuthenticated.value = true
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // TODO: Clear session via Supabase
            _isAuthenticated.value = false
            _isScopedCustomer.value = false
            _uiState.value = AuthUiState()
        }
    }

    /**
     * Convert phone numbers to email format matching web app pattern:
     * 1234567890 -> 1234567890@loanapp.local
     */
    private fun normalizeIdentifier(value: String): String {
        val trimmed = value.trim()
        
        // If it looks like a phone number (digits only, 10 digits)
        if (trimmed.all { it.isDigit() } && trimmed.length == 10) {
            return "$trimmed@loanapp.local"
        }
        
        // Otherwise treat as email
        return trimmed.lowercase()
    }
}
