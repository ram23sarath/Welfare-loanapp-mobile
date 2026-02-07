package com.ijreddy.loanapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.repository.AuthRepository
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class AdminDashboardState(
    val activeCustomers: Int = 0,
    val totalLoans: Int = 0
)

data class CustomerDashboardState(
    val totalOutstanding: Double = 0.0,
    val activeLoans: Int = 0,
    val activeSubscriptions: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    authRepository: AuthRepository,
    customerRepository: CustomerRepository,
    loanRepository: LoanRepository,
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val adminState: StateFlow<AdminDashboardState> = combine(
        customerRepository.customers,
        loanRepository.loans
    ) { customers, loans ->
        AdminDashboardState(
            activeCustomers = customers.size,
            totalLoans = loans.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminDashboardState())

    val customerState: StateFlow<CustomerDashboardState> = combine(
        authRepository.scopedCustomerId,
        loanRepository.loans,
        subscriptionRepository.subscriptions
    ) { scopedCustomerId, loans, subscriptions ->
        if (scopedCustomerId.isNullOrBlank()) {
            CustomerDashboardState()
        } else {
            val customerLoans = loans.filter { it.customer_id == scopedCustomerId }
            val customerSubscriptions = subscriptions.filter { it.customer_id == scopedCustomerId }
            val totalOutstanding = customerLoans.fold(BigDecimal.ZERO) { acc, loan ->
                val interest = BigDecimal.valueOf(loan.principal)
                    .multiply(BigDecimal.valueOf(loan.interest_rate))
                    .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP)
                acc.add(BigDecimal.valueOf(loan.principal).add(interest))
            }
            CustomerDashboardState(
                totalOutstanding = totalOutstanding.toDouble(),
                activeLoans = customerLoans.size,
                activeSubscriptions = customerSubscriptions.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerDashboardState())
}
