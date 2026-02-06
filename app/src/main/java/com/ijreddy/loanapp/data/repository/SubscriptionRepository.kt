package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.SubscriptionDao
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.local.entity.asExternalModel
import com.ijreddy.loanapp.data.sync.SyncManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing subscriptions.
 * Supports offline-first data access and synchronization.
 */
@Singleton

class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val postgrest: Postgrest,
    private val syncManager: SyncManager
) {
    // Expose as flow
    val subscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getActive()
    
    // Calculate total daily subscriptions
    val dailyTotal: Flow<Double> = subscriptions.map { list ->
        list.sumOf { it.amount }
    }
    
    /**
     * Add new subscription with optimistic update.
     */
    suspend fun add(
        customerId: String,
        amount: Double
    ): Result<SubscriptionEntity> {
        return try {
            val entity = SubscriptionEntity(
                id = java.util.UUID.randomUUID().toString(),
                customer_id = customerId,
                amount = amount,
                start_date = java.time.LocalDate.now().toString(),
                created_at = java.time.OffsetDateTime.now().toString()
            )
            
            // Local insert
            subscriptionDao.insert(entity)
            
            // Queue for sync
            syncManager.queueOperation("subscriptions", entity.id, "INSERT", entity)
            
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            subscriptionDao.permanentDelete(id)
            syncManager.queueOperation<Any?>("subscriptions", id, "DELETE", null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
