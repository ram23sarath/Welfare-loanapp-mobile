package com.ijreddy.loanapp.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Customer List screen.
 * Addresses P0 hotspot: 117KB CustomerListPage.tsx
 * - Memoized filtering
 * - Pagination-ready
 * - Single modal state (not per-row)
 */
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Single modal state (P1 optimization: not modal per row)
    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()
    
    // Raw customers from database
    private val rawCustomers: Flow<List<CustomerEntity>> = customerRepository.customers
    
    // Memoized filtered list (avoids recalculation on every render)
    val customers: StateFlow<List<CustomerEntity>> = combine(
        rawCustomers,
        searchQuery
    ) { customerList, query ->
        if (query.isBlank()) {
            customerList
        } else {
            customerList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                customer.phone.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Pre-computed count
    val totalCustomers: StateFlow<Int> = rawCustomers.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }
    
    fun dismissCustomerDetail() {
        _selectedCustomer.value = null
    }
    
    suspend fun addCustomer(name: String, phone: String): Result<CustomerEntity> {
        return customerRepository.add(name, phone)
    }
    
    suspend fun deleteCustomer(customerId: String): Result<Unit> {
        return customerRepository.softDelete(customerId)
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                customerRepository.syncFromRemote()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
