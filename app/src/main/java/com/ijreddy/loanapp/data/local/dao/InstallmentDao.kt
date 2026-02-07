package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for installment operations.
 * Uses deleted_at IS NULL pattern for soft-delete filtering (matching Supabase).
 */
@Dao
interface InstallmentDao {
    
    @Query("SELECT * FROM installments WHERE deleted_at IS NULL ORDER BY date ASC")
    fun getActive(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE loan_id = :loanId AND deleted_at IS NULL ORDER BY date ASC")
    fun getByLoanId(loanId: String): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: String): InstallmentEntity?
    
    @Query("SELECT * FROM installments WHERE status = 'overdue' AND deleted_at IS NULL")
    fun getOverdue(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE status = 'pending' AND date <= :date AND deleted_at IS NULL")
    suspend fun getPendingBefore(date: String): List<InstallmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<InstallmentEntity>)
    
    @Update
    suspend fun update(installment: InstallmentEntity)
    
    @Query("UPDATE installments SET status = 'paid' WHERE id = :id")
    suspend fun markPaid(id: String)
    
    @Query("UPDATE installments SET status = 'overdue' WHERE status = 'pending' AND date < :today AND deleted_at IS NULL")
    suspend fun markOverdueInstallments(today: String)
    
    @Query("UPDATE installments SET deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE installments SET deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM installments WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM installments")
    suspend fun deleteAll()
}

