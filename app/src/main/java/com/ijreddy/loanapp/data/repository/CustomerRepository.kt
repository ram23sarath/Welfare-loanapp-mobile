package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.CustomerDao
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.sync.SyncManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for customer operations.
 * Provides offline-first with Supabase sync.
 */
@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val postgrest: Postgrest,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) {
    val customers: Flow<List<CustomerEntity>> = customerDao.getActive()
    val deletedCustomers: Flow<List<CustomerEntity>> = customerDao.getDeleted()
    
    suspend fun getById(id: String): CustomerEntity? = customerDao.getById(id)
    
    suspend fun add(name: String, phone: String): Result<CustomerEntity> {
        return try {
            val customer = CustomerEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                created_at = Instant.now().toString()
            )
            
            // Insert locally first
            customerDao.insert(customer)
            
            // Queue for sync
            syncManager.queueOperation("customers", customer.id, "INSERT", customer)
            
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun update(id: String, name: String?, phone: String?): Result<CustomerEntity> {
        return try {
            val existing = customerDao.getById(id) ?: throw Exception("Customer not found")
            val updated = existing.copy(
                name = name ?: existing.name,
                phone = phone ?: existing.phone
            )
            
            customerDao.update(updated)
            
            val payload = buildMap {
                if (name != null) put("name", name)
                if (phone != null) put("phone", phone)
            }
            
            if (payload.isNotEmpty()) {
                syncManager.queueOperation("customers", id, "UPDATE", payload)
            }
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            val userId = authRepository.getCurrentUserId() ?: "unknown"
            
            customerDao.softDelete(id, now, userId)
            
            val payload = mapOf(
                "is_deleted" to true,
                "deleted_at" to now,
                "deleted_by" to userId
            )
            syncManager.queueOperation("customers", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restore(id: String): Result<Unit> {
        return try {
            customerDao.restore(id)
            
            val payload = mapOf(
                "is_deleted" to false,
                "deleted_at" to null,
                "deleted_by" to null
            )
            syncManager.queueOperation("customers", id, "UPDATE", payload)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentDelete(id: String): Result<Unit> {
        return try {
            customerDao.permanentDelete(id)
            syncManager.queueOperation<Any?>("customers", id, "DELETE", null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncFromRemote() {
        syncManager.refreshAll()
    }
}
