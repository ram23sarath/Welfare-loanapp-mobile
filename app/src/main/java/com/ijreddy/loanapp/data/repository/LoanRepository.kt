package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.LoanDao
import com.ijreddy.loanapp.data.local.entity.LoanEntity
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
    private val authRepository: AuthRepository
) {
    val loans: Flow<List<LoanEntity>> = loanDao.getActive()
    val deletedLoans: Flow<List<LoanEntity>> = loanDao.getDeleted()
    
    fun getByCustomerId(customerId: String): Flow<List<LoanEntity>> =
        loanDao.getByCustomerId(customerId)
    
    suspend fun getById(id: String): LoanEntity? = loanDao.getById(id)
    
    suspend fun add(
        customerId: String,
        principal: Double,
        interestRate: Double,
        startDate: String,
        tenureMonths: Int,
        installmentAmount: Double
    ): Result<LoanEntity> {
        return try {
            val loan = LoanEntity(
                id = UUID.randomUUID().toString(),
                customer_id = customerId,
                principal = principal,
                interest_rate = interestRate,
                start_date = startDate,
                tenure_months = tenureMonths,
                installment_amount = installmentAmount,
                created_at = Instant.now().toString()
            )
            
            loanDao.insert(loan)
            postgrest.from("loans").insert(loan)
            
            Result.success(loan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun update(id: String, updates: Map<String, Any?>): Result<LoanEntity> {
        return try {
            val existing = loanDao.getById(id) ?: throw Exception("Loan not found")
            val updated = existing.copy(
                principal = (updates["principal"] as? Double) ?: existing.principal,
                interest_rate = (updates["interest_rate"] as? Double) ?: existing.interest_rate,
                status = (updates["status"] as? String) ?: existing.status
            )
            
            loanDao.update(updated)
            postgrest.from("loans").update(updates) { filter { eq("id", id) } }
            
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
            postgrest.from("loans").update({
                set("is_deleted", true)
                set("deleted_at", now)
                set("deleted_by", userId)
            }) { filter { eq("id", id) } }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restore(id: String): Result<Unit> {
        return try {
            loanDao.restore(id)
            postgrest.from("loans").update({
                set("is_deleted", false)
                set("deleted_at", null as String?)
                set("deleted_by", null as String?)
            }) { filter { eq("id", id) } }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentDelete(id: String): Result<Unit> {
        return try {
            loanDao.permanentDelete(id)
            postgrest.from("loans").delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncFromRemote() {
        try {
            val remote = postgrest.from("loans").select().decodeList<LoanEntity>()
            loanDao.upsertAll(remote)
        } catch (e: Exception) { /* Log error */ }
    }
}
