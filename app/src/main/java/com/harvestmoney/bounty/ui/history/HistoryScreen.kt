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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HistoryViewModelFactory()
    ),
    onNavigateBack: () -> Unit
) {
    val withdrawals by viewModel.withdrawals.collectAsState()

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

@OptIn(ExperimentalMaterial3Api::class)
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
                    text = "$${withdrawal.amount}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = withdrawal.status,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (withdrawal.status) {
                        "Completed" -> MaterialTheme.colorScheme.primary
                        "Pending" -> MaterialTheme.colorScheme.tertiary
                        "Rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = withdrawal.paymentMethod,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dateFormatter.format(Date(withdrawal.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )
            if (withdrawal.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = withdrawal.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

class HistoryViewModel : ViewModel() {
    private val _withdrawals = MutableStateFlow<List<Withdrawal>>(emptyList())
    val withdrawals: StateFlow<List<Withdrawal>> = _withdrawals

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    init {
        loadWithdrawals()
    }

    private fun loadWithdrawals() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val withdrawalsRef = database.getReference("withdrawals")
                    .orderByChild("userId")
                    .equalTo(userId)

                val snapshot = withdrawalsRef.get().await()
                val withdrawalsList = mutableListOf<Withdrawal>()

                for (child in snapshot.children) {
                    child.getValue(Withdrawal::class.java)?.let {
                        withdrawalsList.add(it)
                    }
                }

                _withdrawals.value = withdrawalsList.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

class HistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class Withdrawal(
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val timestamp: Long = 0,
    val notes: String = ""
)
