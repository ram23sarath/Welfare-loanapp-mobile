package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.CustomerInterestDao
import com.ijreddy.loanapp.data.local.entity.CustomerInterestEntity
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing customer interest data.
 * Handles fetching and updating interest records.
 */
@Singleton
class CustomerInterestRepository @Inject constructor(
    private val customerInterestDao: CustomerInterestDao,
    private val postgrest: Postgrest
) {
    fun observeInterest(customerId: String): Flow<CustomerInterestEntity?> {
        return customerInterestDao.observeByCustomerId(customerId)
    }
    
    suspend fun getInterest(customerId: String): CustomerInterestEntity? {
        return customerInterestDao.getByCustomerId(customerId)
    }
    
    suspend fun sync(customerId: String) {
        try {
            val result = postgrest.from("customer_interest")
                .select {
                    filter { eq("customer_id", customerId) }
                }
                .decodeSingleOrNull<CustomerInterestEntity>()
            
            if (result != null) {
                customerInterestDao.insert(result)
            }
        } catch (e: Exception) {
            // Error syncing
        }
    }
}
