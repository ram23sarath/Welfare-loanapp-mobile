package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.InstallmentDao
import com.ijreddy.loanapp.data.local.dao.PendingSyncDao
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.local.entity.PendingSyncEntity
import com.ijreddy.loanapp.data.sync.FullSyncManager
import com.ijreddy.loanapp.data.sync.NetworkMonitor
import com.ijreddy.loanapp.data.sync.SyncManager
import com.ijreddy.loanapp.data.sync.SyncScheduler
import io.github.jan.supabase.postgrest.Postgrest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstallmentRepositoryTest {

    @Test
    fun `payInstallment inserts paid installment and queues sync`() = runTest {
        val fakeDao = FakeInstallmentDao()
        val postgrest = mockk<Postgrest>(relaxed = true)
        
        // Create real dependencies with mocks for SyncManager
        val fakePendingSyncDao = FakePendingSyncDao()
        val networkMonitor = mockk<NetworkMonitor> {
            every { isOnline } returns flowOf(false)
            coEvery { isCurrentlyOnline() } returns false
        }
        val syncScheduler = mockk<SyncScheduler>(relaxed = true)
        val fullSyncManager = mockk<FullSyncManager>(relaxed = true)
        
        // Create a real SyncManager with fake/mock dependencies
        val syncManager = SyncManager(fullSyncManager, syncScheduler, fakePendingSyncDao, networkMonitor)

        val repository = InstallmentRepository(fakeDao, postgrest, syncManager)

        val result = repository.payInstallment(
            loanId = "loan-1",
            amount = 1200.0,
            paidDate = "2024-02-01"
        )

        assertEquals(true, result.isSuccess)
        assertEquals(1, fakeDao.items.size)
        assertEquals("paid", fakeDao.items.first().status)
        // Verify the sync was queued by checking the fake PendingSyncDao
        assertEquals(1, fakePendingSyncDao.items.size)
        assertEquals("installments", fakePendingSyncDao.items.first().table_name)
        assertEquals("INSERT", fakePendingSyncDao.items.first().operation)
    }

    private class FakeInstallmentDao : InstallmentDao {
        val items = mutableListOf<InstallmentEntity>()

        override fun getActive(): Flow<List<InstallmentEntity>> = flowOf(items)
        override fun getDeleted(): Flow<List<InstallmentEntity>> = flowOf(emptyList())
        override fun getByLoanId(loanId: String): Flow<List<InstallmentEntity>> =
            flowOf(items.filter { it.loan_id == loanId })
        override suspend fun getById(id: String): InstallmentEntity? = items.find { it.id == id }
        override fun getOverdue(): Flow<List<InstallmentEntity>> = flowOf(emptyList())
        override suspend fun getPendingBefore(date: String): List<InstallmentEntity> = emptyList()
        override suspend fun insert(installment: InstallmentEntity) {
            items.add(installment)
        }
        override suspend fun insertAll(installments: List<InstallmentEntity>) {
            items.addAll(installments)
        }
        override suspend fun update(installment: InstallmentEntity) {
            val index = items.indexOfFirst { it.id == installment.id }
            if (index >= 0) items[index] = installment
        }
        override suspend fun markPaid(id: String, paidDate: String) {}
        override suspend fun markOverdueInstallments(today: String) {}
        override suspend fun softDelete(id: String, now: String, userId: String) {}
        override suspend fun restore(id: String) {}
        override suspend fun permanentDelete(id: String) {
            items.removeAll { it.id == id }
        }
        override suspend fun deleteAll() {
            items.clear()
        }
    }

    private class FakePendingSyncDao : PendingSyncDao {
        val items = mutableListOf<PendingSyncEntity>()

        override suspend fun insert(entity: PendingSyncEntity) {
            items.add(entity)
        }
        override suspend fun getAllPending(): List<PendingSyncEntity> = items.toList()
        override suspend fun delete(id: Long) {
            items.removeAll { it.id == id }
        }
        override suspend fun deleteAll() {
            items.clear()
        }
        override fun getPendingCount(): Flow<Int> = flowOf(items.size)
    }
}
