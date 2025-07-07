package com.harvestmoney.bounty.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current)
    ),
    onMenuClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val points by viewModel.points.collectAsState()
    val withdrawalState by viewModel.withdrawalState.collectAsState()

    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("") }
    var accountDetails by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Harvest Money") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Current Points: $points",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(
                    onClick = { viewModel.showRewardedAd() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Watch Ad (+5 points)")
                }

                Button(
                    onClick = { showWithdrawalDialog = true },
                    enabled = points >= 1000,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Withdraw Points (1000 points = $1)")
                }

                // Show interstitial ad periodically
                LaunchedEffect(points) {
                    if (points > 0 && points % 50 == 0) {
                        viewModel.showInterstitialAd()
                    }
                }

                Text(
                    text = "Withdrawal History",
                    style = MaterialTheme.typography.titleMedium
                )

                val withdrawals by viewModel.withdrawalHistory.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(withdrawals) { withdrawal ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Amount: $${withdrawal.amount / 1000}")
                                    Text(
                                        text = withdrawal.status.uppercase(),
                                        color = when (withdrawal.status) {
                                            "pending" -> MaterialTheme.colorScheme.primary
                                            "completed" -> MaterialTheme.colorScheme.secondary
                                            "rejected" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                Text("Method: ${withdrawal.method}")
                                Text(
                                    "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        .format(Date(withdrawal.timestamp))}"
                                )
                            }
                        }
                    }
                }
            }

            AndroidView(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-7816293804229825/3035899826"
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }

    // Withdrawal Dialog
    if (showWithdrawalDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawalDialog = false },
            title = { Text("Withdraw Points") },
            text = {
                Column {
                    RadioButton(
                        selected = selectedMethod == "Binance",
                        onClick = { selectedMethod = "Binance" }
                    )
                    Text("Binance")

                    RadioButton(
                        selected = selectedMethod == "Payeer",
                        onClick = { selectedMethod = "Payeer" }
                    )
                    Text("Payeer")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = accountDetails,
                        onValueChange = { accountDetails = it },
                        label = { Text("Account Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestWithdrawal(selectedMethod, accountDetails)
                        showWithdrawalDialog = false
                    },
                    enabled = selectedMethod.isNotEmpty() && accountDetails.isNotEmpty()
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Snackbar for withdrawal result
    LaunchedEffect(withdrawalState) {
        when (withdrawalState) {
            is WithdrawalState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Withdrawal request submitted successfully!",
                    duration = SnackbarDuration.Short
                )
            }
            is WithdrawalState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (withdrawalState as WithdrawalState.Error).message,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
            }
            else -> Unit
        }
    }
}
