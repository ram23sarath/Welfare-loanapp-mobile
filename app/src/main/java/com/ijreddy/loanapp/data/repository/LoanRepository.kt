package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.LoanDao
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.sync.SyncManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for loan operations.
 */
@Singleton
class LoanRepository @Inject constructor(
    private val loanDao: LoanDao,
    private val postgrest: Postgrest,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) {
    val loans: Flow<List<LoanEntity>> = loanDao.getActive()
    val deletedLoans: Flow<List<LoanEntity>> = loanDao.getDeleted()
    
    fun getByCustomerId(customerId: String): Flow<List<LoanEntity>> =
        loanDao.getByCustomerId(customerId)
    
    suspend fun getById(id: String): LoanEntity? = loanDao.getById(id)
    
    suspend fun add(
        customerId: String,
        originalAmount: Double,
        interestAmount: Double,
        paymentDate: String,
        totalInstalments: Int,
        checkNumber: String? = null
    ): Result<LoanEntity> {
        return try {
            val loan = LoanEntity(
                id = UUID.randomUUID().toString(),
                customer_id = customerId,
                original_amount = originalAmount,
                interest_amount = interestAmount,
                payment_date = paymentDate,
                total_instalments = totalInstalments,
                check_number = checkNumber,
                created_at = Instant.now().toString()
            )
            
            loanDao.insert(loan)
            syncManager.queueOperation("loans", loan.id, "INSERT", loan)
            
            Result.success(loan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun update(id: String, updates: Map<String, Any?>): Result<LoanEntity> {
        return try {
            val existing = loanDao.getById(id) ?: throw Exception("Loan not found")
            val updated = existing.copy(
                original_amount = (updates["original_amount"] as? Double) ?: existing.original_amount,
                interest_amount = (updates["interest_amount"] as? Double) ?: existing.interest_amount,
                check_number = (updates["check_number"] as? String) ?: existing.check_number
            )
            
            loanDao.update(updated)
            syncManager.queueOperation("loans", id, "UPDATE", updates)
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            val userId = authRepository.getCurrentUserId() ?: "unknown"
            
            loanDao.softDelete(id, now, userId)
            
            val payload = mapOf(
                "deleted_at" to now,
                "deleted_by" to userId
            )
            syncManager.queueOperation("loans", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restore(id: String): Result<Unit> {
        return try {
            loanDao.restore(id)
            
            val payload = mapOf(
                "deleted_at" to null,
                "deleted_by" to null
            )
            syncManager.queueOperation("loans", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentDelete(id: String): Result<Unit> {
        return try {
            loanDao.permanentDelete(id)
            syncManager.queueOperation<Any?>("loans", id, "DELETE", null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncFromRemote() {
        syncManager.refreshAll()
    }
}

