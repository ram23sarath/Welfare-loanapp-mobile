package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "customer_interest",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customer_id"], unique = true)
    ]
)
@Serializable
data class CustomerInterestEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val total_interest_charged: Double = 0.0,
    val last_applied_quarter: String?, // ISO date string
    val created_at: String,
    val updated_at: String,
    
    // Sync fields
    val updated_at_local: Long = System.currentTimeMillis(),
    val sync_status: String = "synced"
)
