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
    suspend fun <T> queueOperation(
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
        
        pendingSyncDao.insert(item)
        
        if (networkMonitor.isCurrentlyOnline()) {
            syncScheduler.triggerImmediateSync()
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
