package com.ijreddy.loanapp.ui.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreen(
    onLoanClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    // viewModel: LoanViewModel = hiltViewModel()
) {
    // TODO: Replace with actual data from ViewModel
    val mockLoans = remember {
        listOf(
            MockLoan("1", "John Doe", 50000.0, "2024-01-15"),
            MockLoan("2", "Jane Smith", 75000.0, "2024-02-20"),
            MockLoan("3", "Bob Johnson", 25000.0, "2024-03-10"),
        )
    }
    val isLoading = false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loans)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockLoans) { loan ->
                    LoanCard(
                        loan = loan,
                        onClick = { onLoanClick(loan.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoanCard(
    loan: MockLoan,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = loan.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${String.format("%,.0f", loan.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = loan.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Temporary mock data class - replace with actual entity
data class MockLoan(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String
)
