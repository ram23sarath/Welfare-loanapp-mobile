package com.ijreddy.loanapp.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.DataEntryRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import com.ijreddy.loanapp.ui.model.LoanUiModel
import com.ijreddy.loanapp.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class CustomerDetailUiState(
    val isLoading: Boolean = true,
    val customer: CustomerEntity? = null,
    val customerEmail: String? = null,
    val totalLoansAmount: Double = 0.0,
    val totalSubscriptionsAmount: Double = 0.0,
    val totalDataEntriesAmount: Double = 0.0,
    val loans: List<LoanUiModel> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val entries: List<DataEntryEntity> = emptyList()
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val dataEntryRepository: DataEntryRepository
) : ViewModel() {

    private val customerId: String = checkNotNull(savedStateHandle["customerId"])

    val uiState: StateFlow<CustomerDetailUiState> = combine(
        flow { emit(customerRepository.getById(customerId)) },
        loanRepository.getByCustomerId(customerId),
        subscriptionRepository.getByCustomerId(customerId),
        dataEntryRepository.getByCustomerId(customerId)
    ) { customer, loans, subscriptions, entries ->
        val customerName = customer?.name ?: "Unknown"
        val loansUi = loans.map { it.toUiModel(customerName) }

        val totalLoans = loans.fold(BigDecimal.ZERO) { acc, loan ->
            val interestAmount = BigDecimal.valueOf(loan.principal)
                .multiply(BigDecimal.valueOf(loan.interest_rate))
                .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP)
            acc.add(BigDecimal.valueOf(loan.principal).add(interestAmount))
        }

        CustomerDetailUiState(
            isLoading = false,
            customer = customer,
            customerEmail = customer?.phone?.takeIf { it.isNotBlank() }?.let { "$it@loanapp.local" },
            totalLoansAmount = totalLoans.toDouble(),
            totalSubscriptionsAmount = subscriptions.sumOf { it.amount },
            totalDataEntriesAmount = entries.sumOf { it.amount },
            loans = loansUi,
            subscriptions = subscriptions,
            entries = entries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerDetailUiState())
}
