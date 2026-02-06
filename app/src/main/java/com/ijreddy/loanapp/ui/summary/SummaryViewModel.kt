package com.ijreddy.loanapp.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.dao.DataEntryDao
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.repository.DataEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Summary screen.
 * Pre-computes aggregates at load time (P2 optimization).
 */
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val dataEntryDao: DataEntryDao,
    private val dataEntryRepository: DataEntryRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Pre-computed summary (cached, not calculated on every render)
    private val _summary = MutableStateFlow(Summary())
    val summary: StateFlow<Summary> = _summary.asStateFlow()
    
    // Recent entries for display (limited to 10)
    val recentCredits: StateFlow<List<DataEntryEntity>> = dataEntryDao.getByType("credit")
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val recentDebits: StateFlow<List<DataEntryEntity>> = dataEntryDao.getByType("debit")
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val recentExpenses: StateFlow<List<DataEntryEntity>> = dataEntryDao.getByType("expense")
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        loadSummary()
    }
    
    /**
     * Load summary using SQL aggregates (efficient).
     */
    private fun loadSummary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (credits, debits, expenses) = dataEntryRepository.getSummary()
                _summary.value = Summary(
                    totalCredits = credits,
                    totalDebits = debits,
                    totalExpenses = expenses,
                    netTotal = credits - debits - expenses
                )
            } catch (e: Exception) {
                // Keep default values on error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadSummary()
    }
}

data class Summary(
    val totalCredits: Double = 0.0,
    val totalDebits: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netTotal: Double = 0.0
)
