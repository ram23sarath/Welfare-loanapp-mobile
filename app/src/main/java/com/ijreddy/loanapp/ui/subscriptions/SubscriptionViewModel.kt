package com.ijreddy.loanapp.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiModel(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val lateFee: Double?
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    val subscriptions: StateFlow<List<SubscriptionUiModel>> = combine(
        subscriptionRepository.subscriptions,
        customerRepository.customers,
        _searchQuery
    ) { subs, customers, query ->
        val customerMap = customers.associateBy { it.id }
        subs.map { sub ->
            SubscriptionUiModel(
                id = sub.id,
                customerName = customerMap[sub.customer_id]?.name ?: "Unknown",
                amount = sub.amount,
                date = sub.date,
                receiptNumber = sub.receipt_number ?: "", // Assuming receipt_number exists in entity
                lateFee = sub.late_fee
            )
        }.filter { 
            query.isBlank() || it.customerName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun softDelete(id: String) {
        viewModelScope.launch {
            subscriptionRepository.softDelete(id)
        }
    }
}
