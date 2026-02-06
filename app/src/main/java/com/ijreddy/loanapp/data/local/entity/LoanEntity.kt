package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Loan entity matching Supabase loans table.
 */
@Serializable
@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customer_id")]
)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val principal: Double,
    val interest_rate: Double,
    val start_date: String,
    val tenure_months: Int,
    val installment_amount: Double,
    val status: String = "active", // active, completed, defaulted
    val created_at: String,
    val is_deleted: Boolean = false,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)
