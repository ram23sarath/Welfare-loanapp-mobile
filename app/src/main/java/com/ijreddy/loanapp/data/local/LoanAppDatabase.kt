package com.ijreddy.loanapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ijreddy.loanapp.data.local.dao.*
import com.ijreddy.loanapp.data.local.entity.*

/**
 * Room database for LoanApp local storage.
 * Provides offline-first capability with sync to Supabase.
 */
@Database(
    entities = [
        CustomerEntity::class,
        LoanEntity::class,
        SubscriptionEntity::class,
        InstallmentEntity::class,
        DataEntryEntity::class,
        LoanSeniorityEntity::class,
        PendingSyncEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LoanAppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun loanDao(): LoanDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun dataEntryDao(): DataEntryDao
    abstract fun seniorityDao(): SeniorityDao
    abstract fun pendingSyncDao(): PendingSyncDao
}
