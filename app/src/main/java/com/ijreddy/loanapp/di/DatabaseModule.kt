package com.ijreddy.loanapp.di

import android.content.Context
import androidx.room.Room
import com.ijreddy.loanapp.data.local.LoanAppDatabase
import com.ijreddy.loanapp.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LoanAppDatabase {
        return Room.databaseBuilder(
            context,
            LoanAppDatabase::class.java,
            "loanapp.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideCustomerDao(db: LoanAppDatabase): CustomerDao = db.customerDao()
    
    @Provides
    fun provideLoanDao(db: LoanAppDatabase): LoanDao = db.loanDao()
    
    @Provides
    fun provideSubscriptionDao(db: LoanAppDatabase): SubscriptionDao = db.subscriptionDao()
    
    @Provides
    fun provideInstallmentDao(db: LoanAppDatabase): InstallmentDao = db.installmentDao()
    
    @Provides
    fun provideDataEntryDao(db: LoanAppDatabase): DataEntryDao = db.dataEntryDao()
    
    @Provides
    fun provideSeniorityDao(db: LoanAppDatabase): SeniorityDao = db.seniorityDao()
    
    @Provides
    fun providePendingSyncDao(db: LoanAppDatabase): PendingSyncDao = db.pendingSyncDao()
}
