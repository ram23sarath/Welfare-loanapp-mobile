package com.ijreddy.loanapp.data.sync

import com.ijreddy.loanapp.data.local.dao.PendingSyncDao
import com.ijreddy.loanapp.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified entry point for data synchronization.
 * Handles queueing local changes, triggering background syncs, and full refreshes.
 */
@Singleton
class SyncManager @Inject constructor(
    private val fullSyncManager: FullSyncManager,
    private val syncScheduler: SyncScheduler,
    private val pendingSyncDao: PendingSyncDao,
    private val networkMonitor: NetworkMonitor
) {
    
    val isOnline: Flow<Boolean> = networkMonitor.isOnline
    
    /**
     * Queue a local change for synchronization.
     * Automatically triggers immediate sync if online.
     */
    @PublishedApi
    internal val pendingSyncDaoInternal: PendingSyncDao
        get() = pendingSyncDao

    @PublishedApi
    internal val networkMonitorInternal: NetworkMonitor
        get() = networkMonitor

    @PublishedApi
    internal val syncSchedulerInternal: SyncScheduler
        get() = syncScheduler

    /**
     * Queue a local change for synchronization.
     * Automatically triggers immediate sync if online.
     */
    suspend inline fun <reified T> queueOperation(
        table: String,
        recordId: String,
        operation: String, // INSERT, UPDATE, DELETE
        payload: T
    ) {
        val jsonPayload = Json.encodeToString(payload)
        
        val item = PendingSyncEntity(
            table_name = table,
            record_id = recordId,
            operation = operation,
            payload = jsonPayload
        )
        
        pendingSyncDaoInternal.insert(item)
        
        if (networkMonitorInternal.isCurrentlyOnline()) {
            syncSchedulerInternal.triggerImmediateSync()
        }
    }
    
    /**
     * Trigger a full sync of all data from remote.
     */
    suspend fun refreshAll(): Result<Unit> {
        return fullSyncManager.syncAll()
    }
    
    /**
     * Schedule periodic background sync.
     */
    fun schedulePeriodicSync() {
        syncScheduler.schedulePeriodicSync()
    }
}
