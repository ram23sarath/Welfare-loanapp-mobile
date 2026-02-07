package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.DataEntryDao
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.sync.SyncManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for data entry (credit/debit/expense) operations.
 */
@Singleton
class DataEntryRepository @Inject constructor(
    private val dataEntryDao: DataEntryDao,
    private val postgrest: Postgrest,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) {
    val entries: Flow<List<DataEntryEntity>> = dataEntryDao.getActive()
    val deletedEntries: Flow<List<DataEntryEntity>> = dataEntryDao.getDeleted()
    
    fun getByType(type: String): Flow<List<DataEntryEntity>> = dataEntryDao.getByType(type)

    fun getByCustomerId(customerId: String): Flow<List<DataEntryEntity>> = dataEntryDao.getByCustomerId(customerId)
    
    suspend fun getSummary(): Triple<Double, Double, Double> {
        val credits = dataEntryDao.getTotalCredits() ?: 0.0
        val debits = dataEntryDao.getTotalDebits() ?: 0.0
        val expenses = dataEntryDao.getTotalExpenses() ?: 0.0
        return Triple(credits, debits, expenses)
    }
    
    suspend fun add(
        customerId: String?,
        amount: Double,
        type: String,
        description: String,
        date: String,
        category: String? = null
    ): Result<DataEntryEntity> {
        return try {
            val entry = DataEntryEntity(
                id = UUID.randomUUID().toString(),
                customer_id = customerId,
                type = type,
                amount = amount,
                description = description,
                date = date,
                category = category,
                created_at = Instant.now().toString()
            )
            
            dataEntryDao.insert(entry)
            syncManager.queueOperation("data_entries", entry.id, "INSERT", entry)
            
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun update(id: String, updates: Map<String, Any?>): Result<DataEntryEntity> {
        return try {
            val existing = dataEntryDao.getById(id) ?: throw Exception("Entry not found")
            val updated = existing.copy(
                amount = (updates["amount"] as? Double) ?: existing.amount,
                description = (updates["description"] as? String) ?: existing.description,
                date = (updates["date"] as? String) ?: existing.date,
                type = (updates["type"] as? String) ?: existing.type,
                category = (updates["category"] as? String) ?: existing.category
            )
            
            dataEntryDao.update(updated)
            syncManager.queueOperation("data_entries", id, "UPDATE", updates)
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            val userId = authRepository.getCurrentUserId() ?: "unknown"
            dataEntryDao.softDelete(id, now, userId)
            
            val payload = mapOf(
                "is_deleted" to true,
                "deleted_at" to now,
                "deleted_by" to userId
            )
            syncManager.queueOperation("data_entries", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restore(id: String): Result<Unit> {
        return try {
            dataEntryDao.restore(id)
            
            val payload = mapOf(
                "is_deleted" to false,
                "deleted_at" to null,
                "deleted_by" to null
            )
            syncManager.queueOperation("data_entries", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentDelete(id: String): Result<Unit> {
        return try {
            dataEntryDao.permanentDelete(id)
            syncManager.queueOperation<Any?>("data_entries", id, "DELETE", null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
