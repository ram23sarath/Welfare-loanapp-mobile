package com.ijreddy.loanapp.data.repository

import android.content.SharedPreferences
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling all authentication operations.
 * Uses Supabase Auth with encrypted local storage for tokens.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val encryptedPrefs: SharedPreferences
) {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: Flow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _isScopedCustomer = MutableStateFlow(false)
    val isScopedCustomer: Flow<Boolean> = _isScopedCustomer.asStateFlow()
    
    private val _scopedCustomerId = MutableStateFlow<String?>(null)
    val scopedCustomerId: Flow<String?> = _scopedCustomerId.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: Flow<UserInfo?> = _currentUser.asStateFlow()
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SCOPED_CUSTOMER_ID = "scoped_customer_id"
    }
    
    /**
     * Initialize session from stored tokens if available.
     */
    suspend fun initializeSession() {
        val accessToken = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        
        if (accessToken != null && refreshToken != null) {
            try {
                // Attempt to restore session
                auth.retrieveUser(accessToken)
                _currentUser.value = auth.currentUserOrNull()
                _isAuthenticated.value = true
                
                // Restore scoped customer state
                val scopedId = encryptedPrefs.getString(KEY_SCOPED_CUSTOMER_ID, null)
                if (scopedId != null) {
                    _scopedCustomerId.value = scopedId
                    _isScopedCustomer.value = true
                } else {
                    checkUserScope()
                }
            } catch (e: Exception) {
                // Token expired or invalid, clear and require re-login
                clearStoredSession()
            }
        }
    }
    
    /**
     * Sign in with phone/email and password.
     * Normalizes phone numbers to email format (e.g., 9876543210 → 9876543210@loanapp.local)
     */
    suspend fun signIn(identifier: String, password: String): Result<Unit> {
        return try {
            val email = normalizeLoginIdentifier(identifier)
            
            android.util.Log.d("AuthRepository", "Sign in attempt - Original: '$identifier', Normalized email: '$email'")
            
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            android.util.Log.d("AuthRepository", "Sign in successful for: $email")
            
            // Store tokens securely
            val session = auth.currentSessionOrNull()
            if (session != null) {
                encryptedPrefs.edit().apply {
                    putString(KEY_ACCESS_TOKEN, session.accessToken)
                    putString(KEY_REFRESH_TOKEN, session.refreshToken)
                    putString(KEY_USER_ID, session.user?.id)
                    apply()
                }
            }
            
            _currentUser.value = auth.currentUserOrNull()
            _isAuthenticated.value = true
            
            // Check if user is a scoped customer
            checkUserScope()
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Sign in failed for '$identifier': ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sign out and clear stored session.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            clearStoredSession()
            
            _isAuthenticated.value = false
            _isScopedCustomer.value = false
            _scopedCustomerId.value = null
            _currentUser.value = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Clear local state even if remote sign out fails
            clearStoredSession()
            _isAuthenticated.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Check if current user is a scoped customer (has matching customers.user_id).
     */
    private suspend fun checkUserScope() {
        val userId = auth.currentUserOrNull()?.id ?: return
        
        try {
            val result = postgrest.from("customers")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<CustomerIdOnly>()
            
            if (result.isNotEmpty()) {
                _isScopedCustomer.value = true
                _scopedCustomerId.value = result.first().id
                
                // Store for faster init next time
                encryptedPrefs.edit()
                    .putString(KEY_SCOPED_CUSTOMER_ID, result.first().id)
                    .apply()
            } else {
                _isScopedCustomer.value = false
                _scopedCustomerId.value = null
            }
        } catch (e: Exception) {
            // Default to non-scoped on error
            _isScopedCustomer.value = false
        }
    }
    
    /**
     * Normalize login identifier to email format.
     * Phone numbers (6-15 digits) → digits@loanapp.local
     * Emails (contains @) → use as-is
     * Usernames without @ → username@loanapp.local
     */
    private fun normalizeLoginIdentifier(value: String): String {
        val v = value.trim()
        
        return when {
            // Already has @, use as-is (it's an email)
            v.contains("@") -> v
            // If input is purely digits (phone number)
            v.all { it.isDigit() } && v.length in 6..15 -> "$v@loanapp.local"
            // Otherwise treat as username
            else -> "$v@loanapp.local"
        }
    }
    
    /**
     * Get the current user's ID if authenticated.
     */
    fun getCurrentUserId(): String? {
        return encryptedPrefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Check if user is currently authenticated (synchronous check).
     */
    fun isLoggedIn(): Boolean = _isAuthenticated.value
    
    private fun clearStoredSession() {
        encryptedPrefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_SCOPED_CUSTOMER_ID)
            apply()
        }
    }
}

@Serializable
private data class CustomerIdOnly(val id: String)
