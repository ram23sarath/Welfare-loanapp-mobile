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
            
            Result.success(Unit)
        } catch (e: Exception) {
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
        return postgrest.from("customers")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchLoans(): List<LoanEntity> {
        return postgrest.from("loans")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchSubscriptions(): List<SubscriptionEntity> {
        return postgrest.from("subscriptions")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchInstallments(): List<InstallmentEntity> {
        return postgrest.from("installments")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchDataEntries(): List<DataEntryEntity> {
        return postgrest.from("data_entries")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchLoanSeniority(): List<LoanSeniorityEntity> {
        return postgrest.from("loan_seniority")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchCustomerInterests(): List<CustomerInterestEntity> {
        return postgrest.from("customer_interest")
            .select()
            .decodeList()
    }
    
    private suspend fun fetchDocuments(): List<DocumentEntity> {
        return postgrest.from("documents")
            .select()
            .decodeList()
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
