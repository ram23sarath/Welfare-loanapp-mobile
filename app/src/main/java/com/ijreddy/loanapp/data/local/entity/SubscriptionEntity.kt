package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Subscription entity matching Supabase subscriptions table.
 */
@Serializable
@Entity(
    tableName = "subscriptions",
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
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val amount: Double,
    val start_date: String,
    val end_date: String? = null,
    val frequency: String = "monthly", // monthly, quarterly, yearly
    val status: String = "active", // active, paused, cancelled
    val paid_amount: Double = 0.0,
    val created_at: String,
    val is_deleted: Boolean = false,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)
