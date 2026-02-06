package com.ijreddy.loanapp.data.session

import android.content.SharedPreferences
import com.ijreddy.loanapp.data.repository.AuthRepository
import com.ijreddy.loanapp.data.sync.FullSyncManager
import com.ijreddy.loanapp.data.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized session manager for app-wide session state.
 * Coordinates auth state, sync triggers, and session lifecycle.
 */
@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val fullSyncManager: FullSyncManager,
    private val syncScheduler: SyncScheduler,
    private val encryptedPrefs: SharedPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Initializing)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    /**
     * Initialize session on app startup.
     * Restores stored session and syncs data if authenticated.
     */
    suspend fun initialize() {
        try {
            _sessionState.value = SessionState.Initializing
            
            // Try to restore session from encrypted storage
            authRepository.initializeSession()
            
            if (authRepository.isLoggedIn()) {
                _sessionState.value = SessionState.Authenticated(
                    userId = authRepository.getCurrentUserId() ?: "",
                    isScopedCustomer = false // Will be updated by flow
                )
                
                // Start background sync
                syncScheduler.schedulePeriodicSync()
                
                // Sync data in background
                scope.launch {
                    fullSyncManager.syncAll()
                }
            } else {
                _sessionState.value = SessionState.Unauthenticated
            }
        } catch (e: Exception) {
            _sessionState.value = SessionState.Unauthenticated
        } finally {
            _isInitialized.value = true
        }
    }
    
    /**
     * Handle successful login.
     */
    suspend fun onLoginSuccess() {
        val userId = authRepository.getCurrentUserId() ?: return
        
        _sessionState.value = SessionState.Authenticated(
            userId = userId,
            isScopedCustomer = false
        )
        
        // Start sync
        syncScheduler.schedulePeriodicSync()
        fullSyncManager.syncAll()
    }
    
    /**
     * Handle logout.
     */
    suspend fun onLogout() {
        // Cancel sync jobs
        syncScheduler.cancelSync()
        
        // Clear local data
        fullSyncManager.clearAllData()
        
        // Sign out
        authRepository.signOut()
        
        _sessionState.value = SessionState.Unauthenticated
    }
    
    /**
     * Refresh session (e.g., on token expiry).
     */
    suspend fun refreshSession(): Boolean {
        return try {
            authRepository.initializeSession()
            authRepository.isLoggedIn()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get last activity timestamp for inactivity timeout.
     */
    fun getLastActivityTime(): Long {
        return encryptedPrefs.getLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
    }
    
    /**
     * Update last activity timestamp.
     */
    fun updateLastActivity() {
        encryptedPrefs.edit()
            .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Check if session is expired due to inactivity.
     */
    fun isSessionExpired(timeoutMinutes: Int = 30): Boolean {
        val lastActivity = getLastActivityTime()
        val now = System.currentTimeMillis()
        val elapsed = now - lastActivity
        return elapsed > (timeoutMinutes * 60 * 1000)
    }
    
    companion object {
        private const val KEY_LAST_ACTIVITY = "last_activity_time"
    }
}

/**
 * Sealed class representing session states.
 */
sealed class SessionState {
    object Initializing : SessionState()
    object Unauthenticated : SessionState()
    data class Authenticated(
        val userId: String,
        val isScopedCustomer: Boolean
    ) : SessionState()
}
