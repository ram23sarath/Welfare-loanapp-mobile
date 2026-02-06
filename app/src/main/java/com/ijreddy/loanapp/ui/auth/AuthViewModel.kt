package com.ijreddy.loanapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel handling authentication state and actions.
 * Exposes session state as StateFlows for Compose observation.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Authentication state
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val isScopedCustomer: StateFlow<Boolean> = authRepository.isScopedCustomer
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val scopedCustomerId: StateFlow<String?> = authRepository.scopedCustomerId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    // UI state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _loginError = MutableSharedFlow<String>()
    val loginError = _loginError.asSharedFlow()
    
    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess = _loginSuccess.asSharedFlow()
    
    init {
        initializeSession()
    }
    
    /**
     * Initialize session from stored tokens on app start.
     */
    private fun initializeSession() {
        viewModelScope.launch {
            try {
                authRepository.initializeSession()
            } catch (e: Exception) {
                // Session initialization failed, user needs to log in
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sign in with phone/email and password.
     */
    fun signIn(identifier: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = authRepository.signIn(identifier, password)
            
            result.fold(
                onSuccess = {
                    _loginSuccess.emit(Unit)
                },
                onFailure = { error ->
                    _loginError.emit(parseAuthError(error))
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * Sign out current user.
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signOut()
            _isLoading.value = false
        }
    }
    
    /**
     * Get current user ID for data queries.
     */
    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()
    
    /**
     * Parse auth errors into user-friendly messages.
     */
    private fun parseAuthError(error: Throwable): String {
        val message = error.message ?: "Authentication failed"
        
        return when {
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "Invalid phone number or password"
            message.contains("Email not confirmed", ignoreCase = true) ->
                "Please verify your email address"
            message.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection."
            message.contains("rate limit", ignoreCase = true) ->
                "Too many attempts. Please try again later."
            else -> message
        }
    }
}
