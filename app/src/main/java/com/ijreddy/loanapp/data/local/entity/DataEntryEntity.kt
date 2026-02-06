package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data entry entity for credits/debits/expenses.
 */
@Serializable
@Entity(tableName = "data_entries")
data class DataEntryEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String? = null,
    val type: String, // credit, debit, expense
    val amount: Double,
    val description: String,
    val date: String,
    val category: String? = null,
    val created_at: String,
    val is_deleted: Boolean = false,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)
