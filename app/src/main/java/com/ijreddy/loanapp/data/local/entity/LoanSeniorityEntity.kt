package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Loan seniority entity for priority list.
 */
@Serializable
@Entity(
    tableName = "loan_seniority",
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
data class LoanSeniorityEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val station_name: String? = null,
    val loan_type: String? = null,
    val loan_request_date: String? = null,
    val position: Int = 0,
    val created_at: String,
    val is_deleted: Boolean = false,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)
