package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Installment entity matching Supabase installments table.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(
    tableName = "installments",
    indices = [Index("loan_id")]
)
data class InstallmentEntity(
    @PrimaryKey
    val id: String,
    val loan_id: String,
    val installment_number: Int? = null,
    val amount: Double,
    val date: String,
    val receipt_number: String? = null,
    val late_fee: Double? = null,
    val status: String = "paid", // pending, paid, overdue
    val created_at: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)

