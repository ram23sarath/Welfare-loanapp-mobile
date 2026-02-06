package com.ijreddy.loanapp.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.ui.model.LoanUiModel
import com.ijreddy.loanapp.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel for Loan List screen.
 * Uses Flow-based observation for efficient updates (P0 optimization).
 */
@HiltViewModel
class LoanListViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val customerRepository: CustomerRepository,
    private val installmentDao: InstallmentDao
) : ViewModel() {
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    // Raw streams
    private val rawLoans = loanRepository.loans
    private val customers = customerRepository.customers

    val customerList: StateFlow<List<CustomerEntity>> = customers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Filtered and sorted loans with customer names
    val loans: StateFlow<List<LoanUiModel>> = combine(
        rawLoans,
        customers,
        searchQuery,
        sortOrder
    ) { loanList, customerList, query, order ->
        val customerMap = customerList.associateBy { it.id }
        
        loanList
            .map { loan ->
                val customerName = customerMap[loan.customer_id]?.name ?: "Unknown"
                loan.toUiModel(customerName)
            }
            .filter { loan ->
                query.isBlank() || 
                loan.customerName.contains(query, ignoreCase = true) ||
                loan.principal.toString().contains(query)
            }
            .let { filtered ->
                when (order) {
                    SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
                    SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.createdAt }
                    SortOrder.AMOUNT_HIGH -> filtered.sortedByDescending { it.principal }
                    SortOrder.AMOUNT_LOW -> filtered.sortedBy { it.principal }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Pre-computed totals
    val totalPrincipal: StateFlow<BigDecimal> = rawLoans.map { loanList ->
        loanList.fold(BigDecimal.ZERO) { acc, loan ->
            acc.add(BigDecimal.valueOf(loan.principal))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)
    
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

    fun addLoan(
        customerId: String,
        principal: BigDecimal,
        interestAmount: BigDecimal,
        startDate: String,
        totalInstallments: Int
    ) {
        viewModelScope.launch {
            val safePrincipal = if (principal > BigDecimal.ZERO) principal else BigDecimal.ZERO
            val interestRate = if (safePrincipal > BigDecimal.ZERO) {
                interestAmount
                    .multiply(BigDecimal.valueOf(100.0))
                    .divide(safePrincipal, 2, java.math.RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            val totalAmount = safePrincipal.add(interestAmount)
            val installmentAmount = if (totalInstallments > 0) {
                totalAmount.divide(BigDecimal.valueOf(totalInstallments.toLong()), 2, java.math.RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            loanRepository.add(
                customerId = customerId,
                principal = safePrincipal.toDouble(),
                interestRate = interestRate.toDouble(),
                startDate = startDate,
                tenureMonths = totalInstallments,
                installmentAmount = installmentAmount.toDouble()
            )
        }
    }
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    AMOUNT_HIGH,
    AMOUNT_LOW
}
