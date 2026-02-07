package com.ijreddy.loanapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for customer operations.
 * Uses deleted_at IS NULL pattern for soft-delete filtering (matching Supabase).
 */
@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers WHERE deleted_at IS NULL ORDER BY name ASC")
    fun getActive(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE phone = :phone AND deleted_at IS NULL LIMIT 1")
    suspend fun getByPhone(phone: String): CustomerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)
    
    @Update
    suspend fun update(customer: CustomerEntity)
    
    @Query("UPDATE customers SET deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE customers SET deleted_at = NULL, deleted_by = NULL WHERE id = :id")
    suspend fun restore(id: String)
    
    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun permanentDelete(id: String)
    
    @Query("DELETE FROM customers")
    suspend fun deleteAll()
    
    @Transaction
    suspend fun upsertAll(customers: List<CustomerEntity>) {
        deleteAll()
        insertAll(customers)
    }
}

