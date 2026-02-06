package com.ijreddy.loanapp.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.DataEntryRepository
import com.ijreddy.loanapp.ui.common.DataEntryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DataEntryUiModel(
    val id: String,
    val customerName: String?,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val type: DataEntryType,
    val notes: String?,
    val subtype: String?
)

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: DataEntryRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedType = MutableStateFlow<DataEntryType?>(null)
    val selectedType = _selectedType.asStateFlow()

    val entries: StateFlow<List<DataEntryUiModel>> = combine(
        repository.entries,
        customerRepository.customers,
        _selectedType
    ) { entriesList, customers, typeFilter ->
        val customerMap = customers.associateBy { it.id }
        
        entriesList
            // Filter by type first
            .filter { entry -> typeFilter == null || DataEntryType.valueOf(entry.type.uppercase()) == typeFilter }
            .map { entry ->
                DataEntryUiModel(
                    id = entry.id,
                    customerName = if (entry.customer_id != null) customerMap[entry.customer_id]?.name ?: "Unknown" else null,
                    amount = entry.amount,
                    date = entry.date,
                    receiptNumber = entry.id.take(8), // Placeholder if receipt isn't stored
                    type = DataEntryType.valueOf(entry.type.uppercase()),
                    notes = entry.notes,
                    subtype = entry.category // Mapping category to subtype
                )
            }
            .sortedByDescending { it.date }
            
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setTypeFilter(type: DataEntryType?) {
        _selectedType.value = type
    }
}
