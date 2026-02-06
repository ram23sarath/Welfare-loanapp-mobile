package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Installment entity matching Supabase installments table.
 */
@Serializable
@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("loan_id")]
)
data class InstallmentEntity(
    @PrimaryKey
    val id: String,
    val loan_id: String,
    val amount: Double,
    val due_date: String,
    val paid_date: String? = null,
    val status: String = "pending", // pending, paid, overdue
    val created_at: String,
    val is_deleted: Boolean = false,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)
