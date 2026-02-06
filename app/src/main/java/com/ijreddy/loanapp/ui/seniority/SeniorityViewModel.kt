package com.ijreddy.loanapp.ui.seniority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.repository.SeniorityRepository
import com.ijreddy.loanapp.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Loan Seniority screen.
 * Addresses P1: LoanSeniorityPage.tsx (60KB)
 */
@HiltViewModel
class SeniorityViewModel @Inject constructor(
    private val seniorityRepository: SeniorityRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Raw seniority list
    val seniorityList: StateFlow<List<LoanSeniorityEntity>> = seniorityRepository.seniorityList
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Customer lookup for names
    val customers: StateFlow<List<CustomerEntity>> = customerRepository.customers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Combined: seniority with customer names
    data class SeniorityWithCustomer(
        val seniority: LoanSeniorityEntity,
        val customerName: String,
        val customerPhone: String
    )
    
    val seniorityWithCustomers: StateFlow<List<SeniorityWithCustomer>> = combine(
        seniorityList,
        customers,
        searchQuery
    ) { seniorities, customerList, query ->
        val customerMap = customerList.associateBy { it.id }
        
        seniorities
            .mapNotNull { seniority ->
                val customer = customerMap[seniority.customer_id]
                customer?.let {
                    SeniorityWithCustomer(
                        seniority = seniority,
                        customerName = it.name,
                        customerPhone = it.phone
                    )
                }
            }
            .filter { item ->
                query.isBlank() ||
                item.customerName.contains(query, ignoreCase = true) ||
                item.customerPhone.contains(query)
            }
            .sortedBy { it.seniority.position }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    suspend fun addToSeniority(
        customerId: String,
        stationName: String?,
        loanType: String?,
        loanRequestDate: String?
    ): Result<LoanSeniorityEntity> {
        return seniorityRepository.add(customerId, stationName, loanType, loanRequestDate)
    }
    
    suspend fun removeFromSeniority(id: String): Result<Unit> {
        return seniorityRepository.softDelete(id)
    }
}
