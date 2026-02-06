package com.ijreddy.loanapp.data.local.dao

import androidx.room.*
import com.ijreddy.loanapp.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY created_at DESC")
    fun observeAll(): Flow<List<DocumentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<DocumentEntity>)
    
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}
