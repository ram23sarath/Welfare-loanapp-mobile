package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.SubscriptionDao
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.local.entity.asExternalModel
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
    private val postgrest: Postgrest
) {
    // Expose as flow
    val subscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.observeAll()
    
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
                date = java.time.LocalDate.now().toString(),
                created_at = java.time.OffsetDateTime.now().toString(),
                sync_status = "pending_create"
            )
            
            // Local insert
            subscriptionDao.insert(entity)
            
            // Remote sync (simplified for now, ideally queue)
            try {
                postgrest.from("subscriptions").insert(entity.asExternalModel())
                subscriptionDao.insert(entity.copy(sync_status = "synced"))
            } catch (e: Exception) {
                // Keep as pending
            }
            
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun softDelete(id: String): Result<Unit> {
        return try {
            subscriptionDao.delete(id)
            // Queue for sync...
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
