package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
    tableName = "customer_interest",
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
    val last_applied_quarter: String? = null, // ISO date string
    val created_at: String? = null,
    val updated_at: String? = null,
    
    // Local-only sync fields - not in Supabase
    @Transient
    val updated_at_local: Long = System.currentTimeMillis(),
    @Transient
    val sync_status: String = "synced"
)

