package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "documents")
@Serializable
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val file_path: String,
    val file_size: Long?,
    val uploaded_by: String?,
    val created_at: String,
    
    // Sync fields (documents are usually read-only or append-only on client)
    val updated_at_local: Long = System.currentTimeMillis()
)
