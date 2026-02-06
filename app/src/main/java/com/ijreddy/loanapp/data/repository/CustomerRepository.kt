package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.CustomerDao
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
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
    private val authRepository: AuthRepository
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
            
            // Sync to Supabase
            postgrest.from("customers").insert(customer)
            
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
            
            postgrest.from("customers").update({
                if (name != null) set("name", name)
                if (phone != null) set("phone", phone)
            }) {
                filter { eq("id", id) }
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
            
            postgrest.from("customers").update({
                set("is_deleted", true)
                set("deleted_at", now)
                set("deleted_by", userId)
            }) {
                filter { eq("id", id) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restore(id: String): Result<Unit> {
        return try {
            customerDao.restore(id)
            
            postgrest.from("customers").update({
                set("is_deleted", false)
                set("deleted_at", null as String?)
                set("deleted_by", null as String?)
            }) {
                filter { eq("id", id) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun permanentDelete(id: String): Result<Unit> {
        return try {
            customerDao.permanentDelete(id)
            
            postgrest.from("customers").delete {
                filter { eq("id", id) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncFromRemote() {
        try {
            val remote = postgrest.from("customers")
                .select()
                .decodeList<CustomerEntity>()
            customerDao.upsertAll(remote)
        } catch (e: Exception) {
            // Log sync failure, data remains stale
        }
    }
}
