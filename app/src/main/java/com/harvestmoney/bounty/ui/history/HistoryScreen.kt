package com.harvestmoney.bounty.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harvestmoney.bounty.ui.home.HomeViewModel
import com.harvestmoney.bounty.ui.home.Withdrawal
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val withdrawals by viewModel.withdrawalHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdrawal History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (withdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No withdrawal history yet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(withdrawals) { withdrawal ->
                    WithdrawalHistoryItem(withdrawal)
                }
            }
        }
    }
}

@Composable
private fun WithdrawalHistoryItem(withdrawal: Withdrawal) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount: $${withdrawal.amount / 1000.0}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = withdrawal.status.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (withdrawal.status.lowercase()) {
                        "completed" -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.tertiary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text("Method: ${withdrawal.method}")
            Text("Account: ${withdrawal.account}")
            Text("Date: ${dateFormatter.format(Date(withdrawal.timestamp))}")
        }
    }
}
