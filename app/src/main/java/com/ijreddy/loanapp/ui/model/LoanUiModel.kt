package com.ijreddy.loanapp.ui.model

import com.ijreddy.loanapp.data.local.entity.LoanEntity

/**
 * UI representation of a Loan with customer details.
 */
data class LoanUiModel(
    val id: String,
    val customerId: String,
    val customerName: String,
    val originalAmount: Double,
    val interestAmount: Double,
    val paymentDate: String,
    val totalInstalments: Int,
    val checkNumber: String?,
    val createdAt: String?
)

fun LoanEntity.toUiModel(customerName: String): LoanUiModel {
    return LoanUiModel(
        id = id,
        customerId = customer_id,
        customerName = customerName,
        originalAmount = original_amount,
        interestAmount = interest_amount,
        paymentDate = payment_date,
        totalInstalments = total_instalments,
        checkNumber = check_number,
        createdAt = created_at
    )
}

