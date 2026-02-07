package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Customer entity matching Supabase customers table.
 * Note: Supabase uses deleted_at = null to indicate active records (soft-delete pattern).
 */
@Serializable
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val created_at: String,
    val user_id: String? = null,
    val station_name: String? = null,
    val deleted_at: String? = null,
    val deleted_by: String? = null
)

