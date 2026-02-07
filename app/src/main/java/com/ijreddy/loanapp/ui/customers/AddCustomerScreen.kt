package com.ijreddy.loanapp.ui.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.ui.components.GlassCard
import com.ijreddy.loanapp.ui.components.LoadingButton
import com.ijreddy.loanapp.ui.components.shake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onNavigateBack: () -> Unit,
    onCustomerAdded: (String) -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    // Shake animation trigger
    var shakeTrigger by remember { mutableStateOf(false) }

    // Effect to trigger shake when error occurs
    LaunchedEffect(uiState.nameError, uiState.phoneError) {
        if (uiState.nameError != null || uiState.phoneError != null) {
            shakeTrigger = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Customer Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                viewModel.clearErrors() 
                            },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.nameError != null,
                            supportingText = {
                                if (uiState.nameError != null) {
                                    Text(uiState.nameError!!)
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { 
                                if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                    phone = it 
                                    viewModel.clearErrors()
                                }
                            },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            prefix = { Text("+91 ") },
                            isError = uiState.phoneError != null,
                            supportingText = {
                                if (uiState.phoneError != null) {
                                    Text(uiState.phoneError!!)
                                } else {
                                    Text("${phone.length}/10 digits")
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.generalError != null) {
                item {
                    Text(
                        text = uiState.generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            item {
                LoadingButton(
                    onClick = {
                         viewModel.submit(
                             name = name.trim(), 
                             phone = phone.trim(),
                             onSuccess = onCustomerAdded
                         )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shake(
                            enabled = shakeTrigger, 
                            onAnimationEnd = { shakeTrigger = false }
                        ),
                    enabled = !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    text = "Save Customer"
                )
            }
        }
    }
}
