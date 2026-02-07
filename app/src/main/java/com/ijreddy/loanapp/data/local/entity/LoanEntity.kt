package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Loan entity matching Supabase loans table.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(
    tableName = "loans",
    indices = [Index("customer_id")]
)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val original_amount: Double,
    val interest_amount: Double,
    val payment_date: String,
    val total_instalments: Int,
    val check_number: String? = null,
    val created_at: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
) {
    // Computed property for backward compatibility with Summary calculations
    val principal: Double get() = original_amount
}

