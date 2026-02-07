package com.ijreddy.loanapp.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijreddy.loanapp.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    data class AddCustomerState(
        val nameError: String? = null,
        val phoneError: String? = null,
        val isSaving: Boolean = false,
        val generalError: String? = null
    )

    private val _uiState = MutableStateFlow(AddCustomerState())
    val uiState: StateFlow<AddCustomerState> = _uiState.asStateFlow()

    fun clearErrors() {
        _uiState.update { it.copy(nameError = null, phoneError = null, generalError = null) }
    }

    fun submit(name: String, phone: String, onSuccess: (customerId: String) -> Unit) {
        val nameError = if (name.isBlank()) "Name cannot be empty" else null
        val phoneError = when {
            phone.isBlank() -> "Phone cannot be empty"
            phone.length != 10 -> "Phone number must be exactly 10 digits"
            else -> null
        }

        if (nameError != null || phoneError != null) {
            _uiState.update { it.copy(nameError = nameError, phoneError = phoneError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null, nameError = null, phoneError = null) }
            val result = customerRepository.add(name, phone)
            
            if (result.isSuccess) {
                val customer = result.getOrNull()
                if (customer != null) {
                    onSuccess(customer.id)
                } else {
                     _uiState.update { it.copy(generalError = "Success but no customer returned") }
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to add customer"
                _uiState.update { it.copy(generalError = error) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
