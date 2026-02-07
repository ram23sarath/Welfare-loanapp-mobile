package com.ijreddy.loanapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ijreddy.loanapp.data.local.dao.CustomerDao
import com.ijreddy.loanapp.data.local.dao.CustomerInterestDao
import com.ijreddy.loanapp.data.local.dao.DataEntryDao
import com.ijreddy.loanapp.data.local.dao.DocumentDao
import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.local.dao.LoanDao
import com.ijreddy.loanapp.data.local.dao.PendingSyncDao
import com.ijreddy.loanapp.data.local.dao.SeniorityDao
import com.ijreddy.loanapp.data.local.dao.SubscriptionDao
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.CustomerInterestEntity
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.local.entity.DocumentEntity
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.LoanSeniorityEntity
import com.ijreddy.loanapp.data.local.entity.PendingSyncEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity

/**
 * Room database for LoanApp local storage.
 * Provides offline-first capability with sync to Supabase.
 * 
 * Version 4: Updated entity schemas to match Supabase tables
 * - Removed is_deleted column (use deleted_at IS NULL pattern)
 * - Updated field names to match Supabase column names
 */
@Database(
    entities = [
        CustomerEntity::class,
        LoanEntity::class,
        SubscriptionEntity::class,
        InstallmentEntity::class,
        DataEntryEntity::class,
        LoanSeniorityEntity::class,
        PendingSyncEntity::class,
        CustomerInterestEntity::class,
        DocumentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LoanAppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun loanDao(): LoanDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun dataEntryDao(): DataEntryDao
    abstract fun seniorityDao(): SeniorityDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun customerInterestDao(): CustomerInterestDao
    abstract fun documentDao(): DocumentDao
}
