package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Loan seniority entity for priority list.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(
    tableName = "loan_seniority",
    indices = [Index("customer_id")]
)
data class LoanSeniorityEntity(
    @PrimaryKey
    val id: String,
    val user_id: String? = null,
    val customer_id: String,
    val station_name: String? = null,
    val loan_type: String? = null,
    val loan_request_date: String? = null,
    val created_at: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)

