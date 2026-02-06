package com.ijreddy.loanapp.ui.model

import com.ijreddy.loanapp.data.local.entity.LoanEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * UI representation of a Loan with customer details.
 */
data class LoanUiModel(
    val id: String,
    val customerId: String,
    val customerName: String,
    val principal: Double,
    val interestRate: Double,
    val startDate: String,
    val tenureMonths: Int,
    val installmentAmount: Double,
    val status: String,
    val createdAt: String
)

fun LoanEntity.toUiModel(customerName: String): LoanUiModel {
    return LoanUiModel(
        id = id,
        customerId = customer_id,
        customerName = customerName,
        principal = principal,
        interestRate = interest_rate,
        startDate = start_date,
        tenureMonths = tenure_months,
        installmentAmount = installment_amount,
        status = status,
        createdAt = created_at
    )
}
