package com.ijreddy.loanapp.data.local.dao

import androidx.room.*
import com.ijreddy.loanapp.data.local.entity.CustomerInterestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerInterestDao {
    @Query("SELECT * FROM customer_interest WHERE customer_id = :customerId")
    fun observeByCustomerId(customerId: String): Flow<CustomerInterestEntity?>
    
    @Query("SELECT * FROM customer_interest WHERE customer_id = :customerId")
    suspend fun getByCustomerId(customerId: String): CustomerInterestEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interest: CustomerInterestEntity)
    
    @Query("DELETE FROM customer_interest WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("DELETE FROM customer_interest")
    suspend fun deleteAll()
}
