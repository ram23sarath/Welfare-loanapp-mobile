package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.SeniorityDao
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for loan seniority list operations.
 */
@Singleton
class SeniorityRepository @Inject constructor(
    private val seniorityDao: SeniorityDao,
    private val postgrest: Postgrest,
    private val authRepository: AuthRepository
) {
    val seniorityList: Flow<List<LoanSeniorityEntity>> = seniorityDao.getActive()
    val deletedSeniority: Flow<List<LoanSeniorityEntity>> = seniorityDao.getDeleted()
    
    suspend fun add(
        customerId: String,
        stationName: String? = null,
        loanType: String? = null,
        loanRequestDate: String? = null
    ): Result<LoanSeniorityEntity> {
        return try {
            // Check for duplicates
            val existing = seniorityDao.getByCustomerId(customerId)
            if (existing != null) {
                throw Exception("Customer already in seniority list")
            }
            
            val maxPosition = seniorityDao.getMaxPosition() ?: 0
            val seniority = LoanSeniorityEntity(
                id = UUID.randomUUID().toString(),
                customer_id = customerId,
                station_name = stationName,
                loan_type = loanType,
                loan_request_date = loanRequestDate,
                position = maxPosition + 1,
                created_at = Instant.now().toString()
            )
            
            seniorityDao.insert(seniority)
            postgrest.from("loan_seniority").insert(seniority)
            
            Result.success(seniority)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun update(
        id: String,
        stationName: String?,
        loanType: String?,
        loanRequestDate: String?
    ): Result<Unit> {
        return try {
            val existing = seniorityDao.getById(id) ?: throw Exception("Entry not found")
            val updated = existing.copy(
                station_name = stationName ?: existing.station_name,
                loan_type = loanType ?: existing.loan_type,
                loan_request_date = loanRequestDate ?: existing.loan_request_date
            )
            
            seniorityDao.update(updated)
            postgrest.from("loan_seniority").update({
                stationName?.let { set("station_name", it) }
                loanType?.let { set("loan_type", it) }
                loanRequestDate?.let { set("loan_request_date", it) }
            }) { filter { eq("id", id) } }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            val userId = authRepository.getCurrentUserId() ?: "unknown"
            
            seniorityDao.softDelete(id, now, userId)
            postgrest.from("loan_seniority").update({
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
            seniorityDao.restore(id)
            postgrest.from("loan_seniority").update({
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
            seniorityDao.permanentDelete(id)
            postgrest.from("loan_seniority").delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
