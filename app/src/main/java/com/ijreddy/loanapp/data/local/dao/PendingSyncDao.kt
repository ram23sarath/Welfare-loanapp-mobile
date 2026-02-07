package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ijreddy.loanapp.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for pending sync queue operations.
 */
@Dao
interface PendingSyncDao {
    
    @Query("SELECT * FROM pending_sync ORDER BY created_at ASC")
    suspend fun getAll(): List<PendingSyncEntity>
    
    @Query("SELECT * FROM pending_sync ORDER BY created_at ASC")
    fun observeAll(): Flow<List<PendingSyncEntity>>
    
    @Query("SELECT COUNT(*) FROM pending_sync")
    fun observeCount(): Flow<Int>
    
    @Query("SELECT * FROM pending_sync WHERE table_name = :table ORDER BY created_at ASC")
    suspend fun getByTable(table: String): List<PendingSyncEntity>
    
    @Insert
    suspend fun insert(sync: PendingSyncEntity): Long
    
    @Query("UPDATE pending_sync SET retry_count = retry_count + 1, last_error = :error WHERE id = :id")
    suspend fun markRetry(id: Long, error: String)
    
    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM pending_sync WHERE record_id = :recordId AND table_name = :table")
    suspend fun deleteByRecord(recordId: String, table: String)
    
    @Query("DELETE FROM pending_sync")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM pending_sync WHERE retry_count >= :maxRetries")
    suspend fun getFailedItems(maxRetries: Int): List<PendingSyncEntity>
}
