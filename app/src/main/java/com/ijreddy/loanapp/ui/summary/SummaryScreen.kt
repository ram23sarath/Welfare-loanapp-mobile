package com.ijreddy.loanapp.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Summary screen showing financial overview with charts.
 * Replaces Chart.js charts with Compose Canvas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    
    // Mock financial data
    val financialData = remember {
        FinancialSummary(
            totalIncome = 850000.0,
            totalExpenses = 125000.0,
            loanDisbursed = 500000.0,
            loanCollected = 425000.0,
            subscriptionTotal = 72000.0,
            interestEarned = 50000.0
        )
    }
    
    val netTotal = financialData.totalIncome - financialData.totalExpenses
    
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
                        containerColor = if (netTotal >= 0) {
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
                            text = currencyFormat.format(netTotal),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (netTotal >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
            
            // Income and Expenses Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Income",
                        amount = currencyFormat.format(financialData.totalIncome),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expenses",
                        amount = currencyFormat.format(financialData.totalExpenses),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Donut Chart
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Income Breakdown",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        DonutChart(
                            values = listOf(
                                ChartValue("Loans Collected", financialData.loanCollected, Color(0xFF6366F1)),
                                ChartValue("Subscriptions", financialData.subscriptionTotal, Color(0xFF10B981)),
                                ChartValue("Interest", financialData.interestEarned, Color(0xFFF59E0B))
                            ),
                            modifier = Modifier.size(200.dp)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Legend
                        ChartLegend(
                            items = listOf(
                                LegendItem("Loans Collected", Color(0xFF6366F1), currencyFormat.format(financialData.loanCollected)),
                                LegendItem("Subscriptions", Color(0xFF10B981), currencyFormat.format(financialData.subscriptionTotal)),
                                LegendItem("Interest", Color(0xFFF59E0B), currencyFormat.format(financialData.interestEarned))
                            )
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
                        
                        StatRow("Disbursed", currencyFormat.format(financialData.loanDisbursed))
                        StatRow("Collected", currencyFormat.format(financialData.loanCollected))
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        StatRow(
                            "Outstanding",
                            currencyFormat.format(financialData.loanDisbursed - financialData.loanCollected),
                            isHighlighted = true
                        )
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
    val total = values.sumOf { it.value }
    
    Canvas(modifier = modifier) {
        val strokeWidth = 40.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = -90f
        
        values.forEach { value ->
            val sweepAngle = (value.value / total * 360).toFloat()
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

private data class FinancialSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val loanDisbursed: Double,
    val loanCollected: Double,
    val subscriptionTotal: Double,
    val interestEarned: Double
)

private data class ChartValue(val label: String, val value: Double, val color: Color)
private data class LegendItem(val label: String, val color: Color, val value: String)
