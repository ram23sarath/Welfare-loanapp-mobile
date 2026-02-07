package com.ijreddy.loanapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "documents")
@Serializable
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val file_path: String,
    val file_size: Long? = null,
    val uploaded_by: String? = null,
    val created_at: String? = null,
    
    // Local-only sync field - not in Supabase
    @Transient
    val updated_at_local: Long = System.currentTimeMillis()
)

