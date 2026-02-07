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
 */
@Dao
interface DataEntryDao {
    
    @Query("SELECT * FROM data_entries WHERE is_deleted = 0 ORDER BY date DESC")
    fun getActive(): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE type = :type AND is_deleted = 0 ORDER BY date DESC")
    fun getByType(type: String): Flow<List<DataEntryEntity>>
    
    @Query("SELECT * FROM data_entries WHERE id = :id")
    suspend fun getById(id: String): DataEntryEntity?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'credit' AND is_deleted = 0")
    suspend fun getTotalCredits(): Double?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'debit' AND is_deleted = 0")
    suspend fun getTotalDebits(): Double?
    
    @Query("SELECT SUM(amount) FROM data_entries WHERE type = 'expense' AND is_deleted = 0")
    suspend fun getTotalExpenses(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DataEntryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DataEntryEntity>)
    
    @Update
    suspend fun update(entry: DataEntryEntity)
    
    @Query("UPDATE data_entries SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE data_entries SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM data_entries WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM data_entries")
    suspend fun deleteAll()
}
