package com.ijreddy.loanapp.data.repository

import com.ijreddy.loanapp.data.local.dao.DocumentDao
import com.ijreddy.loanapp.data.local.entity.DocumentEntity
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing documents (PDFs).
 * Handles Supabase Storage downloads and metadata sync.
 */
@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val postgrest: Postgrest,
    private val storage: Storage
) {
    val documents: Flow<List<DocumentEntity>> = documentDao.observeAll()
    
    suspend fun sync() {
        try {
            val remoteDocs = postgrest.from("documents")
                .select()
                .decodeList<DocumentEntity>()
            
            documentDao.insertAll(remoteDocs)
        } catch (e: Exception) {
            // Offline: keep local data
        }
    }
    
    suspend fun downloadFile(path: String): ByteArray {
        // Implement Supabase Storage download
        return storage.from("documents").downloadPublic(path)
    }
}
