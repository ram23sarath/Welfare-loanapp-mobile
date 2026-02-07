package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Subscription entity matching Supabase subscriptions table.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(
    tableName = "subscriptions",
    indices = [Index("customer_id")]
)
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,
    val customer_id: String,
    val amount: Double,
    val date: String,
    val receipt: String? = null,
    val late_fee: Double? = null,
    val created_at: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)

