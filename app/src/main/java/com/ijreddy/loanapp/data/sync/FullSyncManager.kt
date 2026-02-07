package com.ijreddy.loanapp.data.sync

import com.ijreddy.loanapp.data.local.LoanAppDatabase
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.CustomerInterestEntity
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.local.entity.DocumentEntity
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages full data sync from Supabase to local Room database.
 * Used on login, pull-to-refresh, and periodic background sync.
 */
@Singleton
class FullSyncManager @Inject constructor(
    private val postgrest: Postgrest,
    private val db: LoanAppDatabase
) {
    /**
     * Sync all data from Supabase in dependency order.
     * Uses parallel fetches where possible.
     */
    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("FullSyncManager", "Starting full data sync from Supabase...")
            
            // Phase 1: Fetch all tables in parallel
            val results = awaitAll(
                async { fetchCustomers() },
                async { fetchLoans() },
                async { fetchSubscriptions() },
                async { fetchInstallments() },
                async { fetchDataEntries() },
                async { fetchLoanSeniority() },
                async { fetchCustomerInterests() },
                async { fetchDocuments() }
            )
            
            android.util.Log.d("FullSyncManager", "Fetched data: " +
                "customers=${(results[0] as List<*>).size}, " +
                "loans=${(results[1] as List<*>).size}, " +
                "subscriptions=${(results[2] as List<*>).size}, " +
                "installments=${(results[3] as List<*>).size}, " +
                "dataEntries=${(results[4] as List<*>).size}, " +
                "seniority=${(results[5] as List<*>).size}, " +
                "interests=${(results[6] as List<*>).size}, " +
                "documents=${(results[7] as List<*>).size}")
            
            // Phase 2: Upsert in dependency order (customers first)
            db.customerDao().upsertAll(results[0] as List<CustomerEntity>)
            db.loanDao().upsertAll(results[1] as List<LoanEntity>)
            
            db.subscriptionDao().deleteAll()
            (results[2] as List<SubscriptionEntity>).forEach { db.subscriptionDao().insert(it) }
            
            db.installmentDao().deleteAll()
            (results[3] as List<InstallmentEntity>).forEach { db.installmentDao().insert(it) }
            
            db.dataEntryDao().deleteAll()
            (results[4] as List<DataEntryEntity>).forEach { db.dataEntryDao().insert(it) }
            
            db.seniorityDao().deleteAll()
            (results[5] as List<LoanSeniorityEntity>).forEach { db.seniorityDao().insert(it) }
            
            db.customerInterestDao().deleteAll()
            (results[6] as List<CustomerInterestEntity>).forEach { db.customerInterestDao().insert(it) }
            
            db.documentDao().deleteAll()
            (results[7] as List<DocumentEntity>).forEach { db.documentDao().insert(it) }
            
            android.util.Log.d("FullSyncManager", "Full data sync completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Full data sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync only a specific table.
     */
    suspend fun syncTable(tableName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (tableName) {
                "customers" -> {
                    val data = fetchCustomers()
                    db.customerDao().upsertAll(data)
                }
                "loans" -> {
                    val data = fetchLoans()
                    db.loanDao().upsertAll(data)
                }
                "subscriptions" -> {
                    val data = fetchSubscriptions()
                    db.subscriptionDao().deleteAll()
                    data.forEach { db.subscriptionDao().insert(it) }
                }
                "installments" -> {
                    val data = fetchInstallments()
                    db.installmentDao().deleteAll()
                    data.forEach { db.installmentDao().insert(it) }
                }
                "data_entries" -> {
                    val data = fetchDataEntries()
                    db.dataEntryDao().deleteAll()
                    data.forEach { db.dataEntryDao().insert(it) }
                }
                "loan_seniority" -> {
                    val data = fetchLoanSeniority()
                    db.seniorityDao().deleteAll()
                    data.forEach { db.seniorityDao().insert(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchCustomers(): List<CustomerEntity> {
        return try {
            val result = postgrest.from("customers").select().decodeList<CustomerEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} customers")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch customers: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchLoans(): List<LoanEntity> {
        return try {
            val result = postgrest.from("loans").select().decodeList<LoanEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} loans")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch loans: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchSubscriptions(): List<SubscriptionEntity> {
        return try {
            val result = postgrest.from("subscriptions").select().decodeList<SubscriptionEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} subscriptions")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch subscriptions: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchInstallments(): List<InstallmentEntity> {
        return try {
            val result = postgrest.from("installments").select().decodeList<InstallmentEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} installments")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch installments: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchDataEntries(): List<DataEntryEntity> {
        return try {
            val result = postgrest.from("data_entries").select().decodeList<DataEntryEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} data entries")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch data_entries: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchLoanSeniority(): List<LoanSeniorityEntity> {
        return try {
            val result = postgrest.from("loan_seniority").select().decodeList<LoanSeniorityEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} loan seniority entries")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch loan_seniority: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchCustomerInterests(): List<CustomerInterestEntity> {
        return try {
            val result = postgrest.from("customer_interest").select().decodeList<CustomerInterestEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} customer interests")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch customer_interest: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchDocuments(): List<DocumentEntity> {
        return try {
            val result = postgrest.from("documents").select().decodeList<DocumentEntity>()
            android.util.Log.d("FullSyncManager", "Fetched ${result.size} documents")
            result
        } catch (e: Exception) {
            android.util.Log.e("FullSyncManager", "Failed to fetch documents: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Clear all local data (used on logout).
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.customerDao().deleteAll()
        db.loanDao().deleteAll()
        db.subscriptionDao().deleteAll()
        db.installmentDao().deleteAll()
        db.dataEntryDao().deleteAll()
        db.seniorityDao().deleteAll()
        db.customerInterestDao().deleteAll()
        db.documentDao().deleteAll()
    }
}
