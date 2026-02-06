package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.DataEntryDao
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
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
    private val authRepository: AuthRepository
) {
    val entries: Flow<List<DataEntryEntity>> = dataEntryDao.getActive()
    val deletedEntries: Flow<List<DataEntryEntity>> = dataEntryDao.getDeleted()
    
    fun getByType(type: String): Flow<List<DataEntryEntity>> = dataEntryDao.getByType(type)
    
    suspend fun getSummary(): Triple<Double, Double, Double> {
        val credits = dataEntryDao.getTotalCredits() ?: 0.0
        val debits = dataEntryDao.getTotalDebits() ?: 0.0
        val expenses = dataEntryDao.getTotalExpenses() ?: 0.0
        return Triple(credits, debits, expenses)
    }
    
    suspend fun add(
        type: String,
        amount: Double,
        description: String,
        date: String,
        category: String? = null
    ): Result<DataEntryEntity> {
        return try {
            val entry = DataEntryEntity(
                id = UUID.randomUUID().toString(),
                type = type,
                amount = amount,
                description = description,
                date = date,
                category = category,
                created_at = Instant.now().toString()
            )
            
            dataEntryDao.insert(entry)
            postgrest.from("data_entries").insert(entry)
            
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
                date = (updates["date"] as? String) ?: existing.date
            )
            
            dataEntryDao.update(updated)
            postgrest.from("data_entries").update(updates) { filter { eq("id", id) } }
            
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
            
            postgrest.from("data_entries").update({
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
            dataEntryDao.restore(id)
            postgrest.from("data_entries").update({
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
            dataEntryDao.permanentDelete(id)
            postgrest.from("data_entries").delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
