package com.ijreddy.loanapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.repository.AuthRepository
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.sync.FullSyncManager
import com.ijreddy.loanapp.data.sync.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Dashboard screen.
 * Loads ONLY what's needed for dashboard view (P0 optimization).
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val fullSyncManager: FullSyncManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    // Auth state
    val isScopedCustomer: StateFlow<Boolean> = authRepository.isScopedCustomer
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val scopedCustomerId: StateFlow<String?> = authRepository.scopedCustomerId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    // Network state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    
    // UI state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Data - only load what dashboard needs
    val customers: StateFlow<List<CustomerEntity>> = customerRepository.customers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val loans: StateFlow<List<LoanEntity>> = loanRepository.loans
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Derived: scoped customer info (for customer dashboard)
    val scopedCustomer: StateFlow<CustomerEntity?> = combine(
        scopedCustomerId,
        customers
    ) { id, list ->
        id?.let { customerId -> list.find { it.id == customerId } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    // Derived: customer loan count (for dashboard cards)
    val customerLoanCount: StateFlow<Int> = combine(
        scopedCustomerId,
        loans
    ) { id, loanList ->
        id?.let { customerId -> loanList.count { it.customer_id == customerId } } ?: 0
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    init {
        loadDashboardData()
    }
    
    /**
     * Load only dashboard-relevant data on init.
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Only sync customers and loans for dashboard
                // Other data loaded on-demand by respective screens
                if (networkMonitor.isCurrentlyOnline()) {
                    customerRepository.syncFromRemote()
                    loanRepository.syncFromRemote()
                }
            } catch (e: Exception) {
                // Use cached data if sync fails
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Pull-to-refresh handler.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                fullSyncManager.syncAll()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
