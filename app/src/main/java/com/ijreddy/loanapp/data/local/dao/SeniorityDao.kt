package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for loan seniority list operations.
 * Uses deleted_at IS NULL pattern for soft-delete filtering (matching Supabase).
 */
@Dao
interface SeniorityDao {
    
    @Query("SELECT * FROM loan_seniority WHERE deleted_at IS NULL ORDER BY loan_request_date ASC")
    fun getActive(): Flow<List<LoanSeniorityEntity>>
    
    @Query("SELECT * FROM loan_seniority WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<LoanSeniorityEntity>>
    
    @Query("SELECT * FROM loan_seniority WHERE id = :id")
    suspend fun getById(id: String): LoanSeniorityEntity?
    
    @Query("SELECT * FROM loan_seniority WHERE customer_id = :customerId AND deleted_at IS NULL LIMIT 1")
    suspend fun getByCustomerId(customerId: String): LoanSeniorityEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seniority: LoanSeniorityEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seniorities: List<LoanSeniorityEntity>)
    
    @Update
    suspend fun update(seniority: LoanSeniorityEntity)
    
    @Query("UPDATE loan_seniority SET deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE loan_seniority SET deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM loan_seniority WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM loan_seniority")
    suspend fun deleteAll()
}

