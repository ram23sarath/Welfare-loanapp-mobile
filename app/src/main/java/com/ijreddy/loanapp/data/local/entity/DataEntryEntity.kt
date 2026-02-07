package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data entry entity for credits/debits/expenses.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(tableName = "data_entries")
data class DataEntryEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String? = null,
    val type: String, // credit, debit, expense
    val amount: Double,
    val date: String,
    val receipt_number: String? = null,
    val notes: String? = null,
    val subtype: String? = null,
    val created_at: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)

