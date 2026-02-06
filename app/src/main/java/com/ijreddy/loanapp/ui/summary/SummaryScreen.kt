package com.ijreddy.loanapp.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Summary screen showing financial overview with charts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val summary by viewModel.summary.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Total Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (summary.netTotal >= BigDecimal.ZERO) {
                            Color(0xFF10B981).copy(alpha = 0.15f)
                        } else {
                            Color(0xFFEF4444).copy(alpha = 0.15f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currencyFormat.format(summary.netTotal),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.netTotal >= BigDecimal.ZERO) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
            
            // Credits, Debits, Expenses Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Credits",
                        amount = currencyFormat.format(summary.totalCredits),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Debits",
                        amount = currencyFormat.format(summary.totalDebits),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expenses",
                        amount = currencyFormat.format(summary.totalExpenses),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Collections Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Subscriptions",
                        amount = currencyFormat.format(summary.totalSubscriptions),
                        icon = Icons.Default.Payments,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Interest",
                        amount = currencyFormat.format(summary.totalInterestCharged),
                        icon = Icons.Default.LocalAtm,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Entries",
                        amount = summary.totalEntries.toString(),
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Collections Breakdown
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Collections Breakdown",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        DonutChart(
                            values = listOf(
                                ChartValue("Loans Collected", summary.totalInstallmentsPaid, Color(0xFF6366F1)),
                                ChartValue("Subscriptions", summary.totalSubscriptions, Color(0xFF10B981)),
                                ChartValue("Interest", summary.totalInterestCharged, Color(0xFFF59E0B))
                            ).filter { it.value > BigDecimal.ZERO },
                            modifier = Modifier.size(200.dp)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Legend
                        ChartLegend(
                            items = listOf(
                                Triple("Loans Collected", Color(0xFF6366F1), summary.totalInstallmentsPaid),
                                Triple("Subscriptions", Color(0xFF10B981), summary.totalSubscriptions),
                                Triple("Interest", Color(0xFFF59E0B), summary.totalInterestCharged)
                            )
                                .filter { it.third > BigDecimal.ZERO }
                                .map { (label, color, amount) ->
                                    LegendItem(label, color, currencyFormat.format(amount))
                                }
                        )
                    }
                }
            }
            
            // Loan Stats
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Loan Statistics", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        
                        StatRow("Disbursed", currencyFormat.format(summary.totalLoanPrincipal))
                        StatRow("Collected", currencyFormat.format(summary.totalInstallmentsPaid))
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        StatRow(
                            "Outstanding",
                            currencyFormat.format(summary.loanBalance),
                            isHighlighted = true
                        )
                    }
                }
            }

            // Expense Breakdown
            if (summary.expenseBreakdown.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Expenses by Category", style = MaterialTheme.typography.titleMedium)
                            summary.expenseBreakdown.forEach { item ->
                                StatRow(item.label, currencyFormat.format(item.amount))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DonutChart(
    values: List<ChartValue>,
    modifier: Modifier = Modifier
) {
    val total = values.fold(BigDecimal.ZERO) { acc, value -> acc.add(value.value) }
    if (total == BigDecimal.ZERO) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data")
        }
        return
    }
    
    Canvas(modifier = modifier) {
        val strokeWidth = 40.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = -90f
        
        values.forEach { value ->
            val sweepAngle = value.value.divide(total, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(360.0))
                .toFloat()
            drawArc(
                color = value.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun ChartLegend(items: List<LegendItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = item.color)
                }
                Text(item.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(item.value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class ChartValue(val label: String, val value: BigDecimal, val color: Color)
private data class LegendItem(val label: String, val color: Color, val value: String)
