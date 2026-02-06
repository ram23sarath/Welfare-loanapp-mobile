package com.ijreddy.loanapp.data.local.dao

import androidx.room.*
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for loan seniority list operations.
 */
@Dao
interface SeniorityDao {
    
    @Query("SELECT * FROM loan_seniority WHERE is_deleted = 0 ORDER BY position ASC")
    fun getActive(): Flow<List<LoanSeniorityEntity>>
    
    @Query("SELECT * FROM loan_seniority WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<LoanSeniorityEntity>>
    
    @Query("SELECT * FROM loan_seniority WHERE id = :id")
    suspend fun getById(id: String): LoanSeniorityEntity?
    
    @Query("SELECT * FROM loan_seniority WHERE customer_id = :customerId AND is_deleted = 0 LIMIT 1")
    suspend fun getByCustomerId(customerId: String): LoanSeniorityEntity?
    
    @Query("SELECT MAX(position) FROM loan_seniority WHERE is_deleted = 0")
    suspend fun getMaxPosition(): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seniority: LoanSeniorityEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seniorities: List<LoanSeniorityEntity>)
    
    @Update
    suspend fun update(seniority: LoanSeniorityEntity)
    
    @Query("UPDATE loan_seniority SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE loan_seniority SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM loan_seniority WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM loan_seniority")
    suspend fun deleteAll()
}
