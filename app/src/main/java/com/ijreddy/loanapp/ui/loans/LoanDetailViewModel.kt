package com.ijreddy.loanapp.ui.loans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.InstallmentEntity
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.InstallmentRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.ui.model.LoanUiModel
import com.ijreddy.loanapp.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import javax.inject.Inject

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: LoanRepository,
    customerRepository: CustomerRepository,
    installmentRepository: InstallmentRepository
) : ViewModel() {

    private val loanId: String = checkNotNull(savedStateHandle["loanId"])

    private val loanId: String = checkNotNull(savedStateHandle["loanId"])

    val loan: StateFlow<LoanUiModel?> = combine(
        repository.getById(loanId),
        customerRepository.customers
    ) { loanEntity: LoanEntity?, customers: List<CustomerEntity> ->
        loanEntity?.let {
            val customer = customers.find { c -> c.id == it.customer_id }
            it.toUiModel(customer?.name ?: "Unknown")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val installments: StateFlow<List<InstallmentEntity>> = installmentRepository.getInstallmentsForLoan(loanId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
