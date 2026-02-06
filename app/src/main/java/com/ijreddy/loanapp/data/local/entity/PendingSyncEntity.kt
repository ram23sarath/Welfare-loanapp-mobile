package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks pending sync operations for offline-first architecture.
 * Changes made while offline are queued here and processed by SyncWorker.
 */
@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val table_name: String,        // "customers", "loans", etc.
    val record_id: String,         // UUID of affected record
    val operation: String,         // "INSERT", "UPDATE", "DELETE"
    val payload: String,           // JSON serialized changes
    val created_at: Long = System.currentTimeMillis(),
    val retry_count: Int = 0,
    val last_error: String? = null
)
