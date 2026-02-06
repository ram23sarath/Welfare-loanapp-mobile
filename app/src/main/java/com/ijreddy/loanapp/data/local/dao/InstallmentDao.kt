package com.ijreddy.loanapp.data.local.dao

import androidx.room.*
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for installment operations.
 */
@Dao
interface InstallmentDao {
    
    @Query("SELECT * FROM installments WHERE is_deleted = 0 ORDER BY due_date ASC")
    fun getActive(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE loan_id = :loanId AND is_deleted = 0 ORDER BY due_date ASC")
    fun getByLoanId(loanId: String): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: String): InstallmentEntity?
    
    @Query("SELECT * FROM installments WHERE status = 'overdue' AND is_deleted = 0")
    fun getOverdue(): Flow<List<InstallmentEntity>>
    
    @Query("SELECT * FROM installments WHERE status = 'pending' AND due_date <= :date AND is_deleted = 0")
    suspend fun getPendingBefore(date: String): List<InstallmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<InstallmentEntity>)
    
    @Update
    suspend fun update(installment: InstallmentEntity)
    
    @Query("UPDATE installments SET status = 'paid', paid_date = :paidDate WHERE id = :id")
    suspend fun markPaid(id: String, paidDate: String)
    
    @Query("UPDATE installments SET status = 'overdue' WHERE status = 'pending' AND due_date < :today AND is_deleted = 0")
    suspend fun markOverdueInstallments(today: String)
    
    @Query("UPDATE installments SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE installments SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM installments WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM installments")
    suspend fun deleteAll()
}
