package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for subscription operations.
 */
@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscriptions WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getActive(): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE customer_id = :customerId AND is_deleted = 0")
    fun getByCustomerId(customerId: String): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: String): SubscriptionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<SubscriptionEntity>)
    
    @Update
    suspend fun update(subscription: SubscriptionEntity)
    
    @Query("UPDATE subscriptions SET paid_amount = paid_amount + :amount WHERE id = :id")
    suspend fun addPayment(id: String, amount: Double)
    
    @Query("UPDATE subscriptions SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE subscriptions SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}
