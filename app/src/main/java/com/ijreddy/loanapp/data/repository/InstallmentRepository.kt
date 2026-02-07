package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.sync.SyncManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing loan installments.
 */
@Singleton

class InstallmentRepository @Inject constructor(
    private val installmentDao: InstallmentDao,
    private val postgrest: Postgrest,
    private val syncManager: SyncManager
) {
    val installments: Flow<List<InstallmentEntity>> = installmentDao.getActive()
    
    fun getInstallmentsForLoan(loanId: String): Flow<List<InstallmentEntity>> {
        return installmentDao.getByLoanId(loanId)
    }
    
    suspend fun payInstallment(
        loanId: String,
        amount: Double,
        paidDate: String
    ): Result<InstallmentEntity> {
        return try {
            val installment = InstallmentEntity(
                id = java.util.UUID.randomUUID().toString(),
                loan_id = loanId,
                amount = amount,
                due_date = paidDate,
                paid_date = paidDate,
                status = "paid",
                created_at = java.time.Instant.now().toString()
            )

            installmentDao.insert(installment)
            syncManager.queueOperation("installments", installment.id, "INSERT", installment)

            Result.success(installment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
