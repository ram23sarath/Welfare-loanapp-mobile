package com.ijreddy.loanapp.ui.records

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.InstallmentRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class RecordType { LOAN, SUBSCRIPTION, INSTALLMENT }

data class LoanFormState(
    val originalAmount: String = "",
    val totalRepayable: String = "", // Used to derive interest
    val totalInstallments: String = "12",
    val paymentDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val checkNumber: String = ""
)

data class SubscriptionFormState(
    val amount: String = "",
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val receipt: String = "",
    val lateFee: String = ""
)

data class InstallmentFormState(
    val selectedLoanId: String? = null,
    val amount: String = "",
    val installmentNumber: Int? = null,
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val receipt: String = "",
    val lateFee: String = ""
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AddRecordViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val installmentRepository: InstallmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Parameters
    private val preselectedCustomerId: String? = savedStateHandle["customerId"]

    // UI States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Customer Search
    val searchResults = _searchQuery
        .debounce(300)
        .combine(customerRepository.customers) { query, customers ->
            if (query.isBlank()) emptyList()
            else customers.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.phone.contains(query) 
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    private val _recordType = MutableStateFlow(RecordType.INSTALLMENT) // Default to installment as it's most common? Or Loan? Spec says Add Customer -> Add Record (likely loan). 
    // Usually "Add Record" implies selecting a customer then picking type.
    // If coming from Add Customer, probably Loan.
    val recordType = _recordType.asStateFlow()

    // Forms
    private val _loanForm = MutableStateFlow(LoanFormState())
    val loanForm = _loanForm.asStateFlow()

    private val _subscriptionForm = MutableStateFlow(SubscriptionFormState())
    val subscriptionForm = _subscriptionForm.asStateFlow()

    private val _installmentForm = MutableStateFlow(InstallmentFormState())
    val installmentForm = _installmentForm.asStateFlow()

    // Customer Loans for Installment Form
    private val _customerLoans = MutableStateFlow<List<LoanEntity>>(emptyList())
    val customerLoans = _customerLoans.asStateFlow()

    // Existing installments for validation
    private val _existingInstallments = MutableStateFlow<List<com.ijreddy.loanapp.data.local.entity.InstallmentEntity>>(emptyList())
    val existingInstallments = _existingInstallments.asStateFlow()

    // Status
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    data class UiState(
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    init {
        // Handle preselection
        if (!preselectedCustomerId.isNullOrBlank()) {
            viewModelScope.launch {
                val customer = customerRepository.getById(preselectedCustomerId)
                if (customer != null) {
                    selectCustomer(customer)
                    // If coming from Add Customer, default to LOAN
                    _recordType.value = RecordType.LOAN 
                }
            }
        }
        
        // Observe loan selection to fetch installments
        viewModelScope.launch {
            _installmentForm
                .map { it.selectedLoanId }
                .debounce(300) // Avoid rapid changes
                .collect { loanId ->
                    if (loanId != null) {
                        fetchLoanInstallments(loanId)
                    } else {
                        _existingInstallments.value = emptyList()
                    }
                }
        }
    }

    private fun fetchLoanInstallments(loanId: String) {
        viewModelScope.launch {
             try {
                 // Using first() for simplicity, real app might observe
                 val installments = installmentRepository.getInstallmentsForLoan(loanId).first()
                 _existingInstallments.value = installments
                 
                 // Auto-set next installment number
                 val maxNumber = installments.mapNotNull { it.installment_number }.maxOrNull() ?: 0
                 val next = maxNumber + 1
                 // Check if next is within bounds? We need total installments from loan.
                 val loan = _customerLoans.value.find { it.id == loanId }
                 if (loan != null && next <= loan.total_instalments) {
                      updateInstallmentForm { it.copy(installmentNumber = next) }
                 }
             } catch (e: Exception) {
                 // Ignore
             }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCustomer(customer: CustomerEntity) {
        _selectedCustomer.value = customer
        _searchQuery.value = ""
        fetchCustomerLoans(customer.id)
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _customerLoans.value = emptyList()
        // Reset forms? Maybe keep them.
    }

    fun setRecordType(type: RecordType) {
        _recordType.value = type
    }
    
    private fun fetchCustomerLoans(customerId: String) {
        viewModelScope.launch {
            // Collecting one-shot or keep observing? 
            // In a real app we might observe. For simplicity, just get current loans.
            // LoanRepository has 'loans' Flow<List<Loan>>.
            // We can filter it.
            try {
                // Assuming repository exposes a flow of all loans or by customer
                // Using the dao directly would be better but we have repo.
                // Looking at LoanRepository, it doesn't have getByCustomer directly exposed as list, 
                // but we can query DAOs or filter the flow.
                // For now, let's assume we can filter the flow "loans".
                val allLoans = loanRepository.loans.first() 
                val loans = allLoans.filter { it.customer_id == customerId }
                _customerLoans.value = loans
                
                // Auto-select first loan if available
                if (loans.isNotEmpty()) {
                    updateInstallmentForm { it.copy(selectedLoanId = loans.first().id) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Form Updates
    fun updateLoanForm(newState: LoanFormState) {
        _loanForm.value = newState
    }

    fun updateSubscriptionForm(newState: SubscriptionFormState) {
        _subscriptionForm.value = newState
    }

    fun updateInstallmentForm(newState: InstallmentFormState) {
        _installmentForm.value = newState
    }

    fun submit() {
        val customer = selectedCustomer.value ?: run {
            _uiState.update { it.copy(errorMessage = "Please select a customer") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                val result = when (recordType.value) {
                    RecordType.LOAN -> saveLoan(customer.id)
                    RecordType.SUBSCRIPTION -> saveSubscription(customer.id)
                    RecordType.INSTALLMENT -> saveInstallment()
                }
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(successMessage = "Record saved successfully") }
                    // Reset forms or navigate back?
                } else {
                    _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to save") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                 _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
    
    private suspend fun saveLoan(customerId: String): Result<Unit> {
        val form = loanForm.value
        val original = form.originalAmount.toDoubleOrNull()
        val total = form.totalRepayable.toDoubleOrNull()
        val installments = form.totalInstallments.toIntOrNull()
        
        if (original == null || total == null || installments == null) {
            return Result.failure(Exception("Invalid input values"))
        }
        
        if (total < original) {
            return Result.failure(Exception("Total amount cannot be less than original amount"))
        }

        val interest = total - original
        
        return loanRepository.add(
            customerId = customerId,
            originalAmount = original,
            interestAmount = interest,
            paymentDate = form.paymentDate,
            totalInstalments = installments,
            checkNumber = form.checkNumber.ifBlank { null }
        ).map { }
    }
    
    private suspend fun saveSubscription(customerId: String): Result<Unit> {
        val form = subscriptionForm.value
        val amount = form.amount.toDoubleOrNull()
        
        if (amount == null) return Result.failure(Exception("Invalid amount"))
        
        return subscriptionRepository.add(
            customerId = customerId,
            amount = amount,
            date = form.date,
            receipt = form.receipt.ifBlank { null },
            lateFee = form.lateFee.toDoubleOrNull()
        ).map { }
    }
    
    private suspend fun saveInstallment(): Result<Unit> {
        val form = installmentForm.value
        val amount = form.amount.toDoubleOrNull()
        val loanId = form.selectedLoanId
        
        if (amount == null || loanId == null) return Result.failure(Exception("Invalid values"))
        
        return installmentRepository.payInstallment(
            loanId = loanId,
            amount = amount,
            date = form.date,
            receiptNumber = form.receipt.ifBlank { null },
            installmentNumber = form.installmentNumber,
            lateFee = form.lateFee.toDoubleOrNull()
        ).map { }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
