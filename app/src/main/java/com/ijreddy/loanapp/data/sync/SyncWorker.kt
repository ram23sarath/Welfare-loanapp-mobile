package com.ijreddy.loanapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ijreddy.loanapp.data.local.dao.PendingSyncDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * WorkManager worker for syncing pending changes to Supabase.
 * Processes queued operations with retry and exponential backoff.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingSyncDao: PendingSyncDao,
    private val postgrest: Postgrest
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = pendingSyncDao.getAll()
        if (pending.isEmpty()) {
            return Result.success()
        }

        var failures = 0

        for (item in pending) {
            try {
                when (item.operation) {
                    OPERATION_INSERT -> syncInsert(item.table_name, item.payload)
                    OPERATION_UPDATE -> syncUpdate(item.table_name, item.record_id, item.payload)
                    OPERATION_DELETE -> syncDelete(item.table_name, item.record_id)
                }
                pendingSyncDao.delete(item.id)
            } catch (e: Exception) {
                failures++
                if (item.retry_count >= MAX_RETRIES) {
                    // Move to dead letter queue (logged, not retried)
                    pendingSyncDao.delete(item.id)
                    // TODO: Log to analytics or notify user
                } else {
                    pendingSyncDao.markRetry(item.id, e.message ?: "Unknown error")
                }
            }
        }

        return if (failures > 0 && failures < pending.size) {
            // Partial success, retry remaining
            Result.retry()
        } else if (failures == pending.size) {
            // All failed
            Result.failure()
        } else {
            Result.success()
        }
    }

    private suspend fun syncInsert(table: String, payload: String) {
        val jsonObject = Json.decodeFromString<JsonObject>(payload)
        postgrest.from(table).insert(jsonObject)
    }

    private suspend fun syncUpdate(table: String, recordId: String, payload: String) {
        val jsonObject = Json.decodeFromString<JsonObject>(payload)
        postgrest.from(table).update(jsonObject) {
            filter { eq("id", recordId) }
        }
    }

    private suspend fun syncDelete(table: String, recordId: String) {
        postgrest.from(table).delete {
            filter { eq("id", recordId) }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        const val WORK_NAME = "loanapp_sync"
        private const val OPERATION_INSERT = "INSERT"
        private const val OPERATION_UPDATE = "UPDATE"
        private const val OPERATION_DELETE = "DELETE"
    }
}
