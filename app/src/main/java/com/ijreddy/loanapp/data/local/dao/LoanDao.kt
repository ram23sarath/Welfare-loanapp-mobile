package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for loan operations.
 */
@Dao
interface LoanDao {
    
    @Query("SELECT * FROM loans WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getActive(): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE customer_id = :customerId AND is_deleted = 0")
    fun getByCustomerId(customerId: String): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getById(id: String): LoanEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(loans: List<LoanEntity>)
    
    @Update
    suspend fun update(loan: LoanEntity)
    
    @Query("UPDATE loans SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE loans SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM loans")
    suspend fun deleteAll()
    
    @Transaction
    suspend fun upsertAll(loans: List<LoanEntity>) {
        deleteAll()
        insertAll(loans)
    }
}
