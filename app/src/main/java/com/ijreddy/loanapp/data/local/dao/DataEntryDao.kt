package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for data entry (credit/debit/expense) operations.
 * Uses deleted_at IS NULL pattern for soft-delete filtering (matching Supabase).
 */
@Dao
interface DataEntryDao {
    
    @Query("SELECT * FROM data_entries WHERE deleted_at IS NULL ORDER BY date DESC")
    fun getActive(): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE type = :type AND deleted_at IS NULL ORDER BY date DESC")
    fun getByType(type: String): Flow<List<DataEntryEntity>>

    @Query("SELECT * FROM data_entries WHERE customer_id = :customerId AND deleted_at IS NULL ORDER BY date DESC")
    fun getByCustomerId(customerId: String): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE id = :id")
    suspend fun getById(id: String): DataEntryEntity?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'credit' AND deleted_at IS NULL")
    suspend fun getTotalCredits(): Double?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'debit' AND deleted_at IS NULL")
    suspend fun getTotalDebits(): Double?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'expense' AND deleted_at IS NULL")
    suspend fun getTotalExpenses(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DataEntryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DataEntryEntity>)
    
    @Update
    suspend fun update(entry: DataEntryEntity)
    
    @Query("UPDATE data_entries SET deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE data_entries SET deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM data_entries WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM data_entries")
    suspend fun deleteAll()
}

