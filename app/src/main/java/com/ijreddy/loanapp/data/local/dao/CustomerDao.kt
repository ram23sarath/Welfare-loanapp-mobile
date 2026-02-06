package com.ijreddy.loanapp.data.local.dao

import androidx.room.*
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for customer operations.
 */
@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers WHERE is_deleted = 0 ORDER BY name ASC")
    fun getActive(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeleted(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE phone = :phone AND is_deleted = 0 LIMIT 1")
    suspend fun getByPhone(phone: String): CustomerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)
    
    @Update
    suspend fun update(customer: CustomerEntity)
    
    @Query("UPDATE customers SET is_deleted = 1, deleted_at = :now, deleted_by = :userId WHERE id = :id")
    suspend fun softDelete(id: String, now: String, userId: String)
    
    @Query("UPDATE customers SET is_deleted = 0, deleted_at = NULL, deleted_by = NULL WHERE id = :id")
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
