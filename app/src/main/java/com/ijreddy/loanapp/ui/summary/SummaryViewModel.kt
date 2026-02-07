package com.ijreddy.loanapp.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.dao.CustomerInterestDao
import com.ijreddy.loanapp.data.local.dao.DataEntryDao
import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.local.dao.LoanDao
import com.ijreddy.loanapp.data.local.dao.SubscriptionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel for Summary screen.
 * Aggregates are derived from local data streams.
 */
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val dataEntryDao: DataEntryDao,
    private val subscriptionDao: SubscriptionDao,
    private val loanDao: LoanDao,
    private val installmentDao: InstallmentDao,
    private val customerInterestDao: CustomerInterestDao
) : ViewModel() {
    val summary: StateFlow<SummaryState> = combine(
        dataEntryDao.getActive(),
        subscriptionDao.getActive(),
        loanDao.getActive(),
        installmentDao.getActive(),
        customerInterestDao.getAll()
    ) { entries, subscriptions, loans, installments, interestRecords ->
        val totalCredits = entries
            .filter { it.type == "credit" }
            .sumAmount { it.amount }
        val totalDebits = entries
            .filter { it.type == "debit" }
            .sumAmount { it.amount }
        val totalExpenses = entries
            .filter { it.type == "expense" }
            .sumAmount { it.amount }

        val totalSubscriptions = subscriptions.sumAmount { it.amount }
        val totalLoanPrincipal = loans.sumAmount { it.principal }
        val totalInstallmentsPaid = installments
            .filter { it.status == "paid" }
            .sumAmount { it.amount }
        val loanBalance = totalLoanPrincipal.subtract(totalInstallmentsPaid)

        val totalInterestCharged = interestRecords.sumAmount { it.total_interest_charged }

        val expenseBreakdown = entries
            .filter { it.type == "expense" }
            .groupBy { entry -> entry.subtype?.takeIf { it.isNotBlank() } ?: "Uncategorized" }
            .map { (label, grouped) ->
                ExpenseBreakdownItem(label, grouped.sumAmount { it.amount })
            }
            .sortedByDescending { it.amount }

        SummaryState(
            totalCredits = totalCredits,
            totalDebits = totalDebits,
            totalExpenses = totalExpenses,
            netTotal = totalCredits.subtract(totalDebits).subtract(totalExpenses),
            totalSubscriptions = totalSubscriptions,
            totalLoanPrincipal = totalLoanPrincipal,
            totalInstallmentsPaid = totalInstallmentsPaid,
            loanBalance = loanBalance,
            totalInterestCharged = totalInterestCharged,
            totalEntries = entries.size,
            expenseBreakdown = expenseBreakdown
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryState())
}

data class SummaryState(
    val totalCredits: BigDecimal = BigDecimal.ZERO,
    val totalDebits: BigDecimal = BigDecimal.ZERO,
    val totalExpenses: BigDecimal = BigDecimal.ZERO,
    val netTotal: BigDecimal = BigDecimal.ZERO,
    val totalSubscriptions: BigDecimal = BigDecimal.ZERO,
    val totalLoanPrincipal: BigDecimal = BigDecimal.ZERO,
    val totalInstallmentsPaid: BigDecimal = BigDecimal.ZERO,
    val loanBalance: BigDecimal = BigDecimal.ZERO,
    val totalInterestCharged: BigDecimal = BigDecimal.ZERO,
    val totalEntries: Int = 0,
    val expenseBreakdown: List<ExpenseBreakdownItem> = emptyList()
)

data class ExpenseBreakdownItem(
    val label: String,
    val amount: BigDecimal
)

private fun Iterable<Double>.sumAmount(): BigDecimal =
    fold(BigDecimal.ZERO) { acc, value -> acc.add(BigDecimal.valueOf(value)) }

private fun <T> Iterable<T>.sumAmount(selector: (T) -> Double): BigDecimal =
    fold(BigDecimal.ZERO) { acc, item -> acc.add(BigDecimal.valueOf(selector(item))) }
