package com.ijreddy.loanapp.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.local.dao.LoanDao
import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Loan List screen.
 * Uses Flow-based observation for efficient updates (P0 optimization).
 */
@HiltViewModel
class LoanListViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val loanDao: LoanDao,
    private val installmentDao: InstallmentDao
) : ViewModel() {
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    // Raw loans from database (reactive)
    private val rawLoans: Flow<List<LoanEntity>> = loanRepository.loans
    
    // Filtered and sorted loans (memoized via combine)
    val loans: StateFlow<List<LoanEntity>> = combine(
        rawLoans,
        searchQuery,
        sortOrder
    ) { loanList, query, order ->
        loanList
            .filter { loan ->
                query.isBlank() || loan.customer_id.contains(query, ignoreCase = true)
            }
            .let { filtered ->
                when (order) {
                    SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.created_at }
                    SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.created_at }
                    SortOrder.AMOUNT_HIGH -> filtered.sortedByDescending { it.principal }
                    SortOrder.AMOUNT_LOW -> filtered.sortedBy { it.principal }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Pre-computed totals (P1 optimization - avoid inline calculations)
    val totalPrincipal: StateFlow<Double> = rawLoans.map { loanList ->
        loanList.sumOf { it.principal }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    val totalLoans: StateFlow<Int> = rawLoans.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    /**
     * Get installments for a specific loan.
     */
    fun getInstallments(loanId: String): Flow<List<InstallmentEntity>> {
        return installmentDao.getByLoanId(loanId)
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
    
    suspend fun deleteLoan(loanId: String) {
        loanRepository.softDelete(loanId)
    }
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    AMOUNT_HIGH,
    AMOUNT_LOW
}
