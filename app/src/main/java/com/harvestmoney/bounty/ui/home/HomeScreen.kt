package com.harvestmoney.bounty.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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
    val snackbarHostState = rememberSnackbarHostState()
    val scope = rememberCoroutineScope()

    val points by viewModel.points.collectAsState()
    val withdrawalState by viewModel.withdrawalState.collectAsState()

    // Prefill account details from profile
    val profile by viewModel.profile.collectAsState()
    var selectedMethod by remember { mutableStateOf(profile.defaultMethod) }
    var accountDetails by remember { mutableStateOf(profile.account) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Money") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
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
                Text("Watch Ad (+${viewModel.rewardAmount} points)")
            }

            Button(
                onClick = { showWithdrawalDialog = true },
                enabled = points >= viewModel.minWithdrawalPoints,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Withdraw Points (${viewModel.minWithdrawalPoints} points = $1)")
            }

            // Interstitial ad on interval
            LaunchedEffect(points) {
                if (points > 0 && points % viewModel.interstitialInterval == 0) {
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
                items(withdrawals) { w ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount: \$${w.amount / viewModel.minWithdrawalPoints}")
                                Text(
                                    text = w.status.uppercase(),
                                    color = when (w.status) {
                                        "pending" -> MaterialTheme.colorScheme.primary
                                        "completed" -> MaterialTheme.colorScheme.secondary
                                        "rejected" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            Text("Method: ${w.method}")
                            Text(
                                text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(w.timestamp))}"
                            )
                        }
                    }
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-7816293804229825/3035899826"
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }

        if (showWithdrawalDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawalDialog = false },
                title = { Text("Withdraw Points") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = {}
                        ) {
                            listOf("Binance", "Payeer").forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = { selectedMethod = method }
                                )
                            }
                        }
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

        LaunchedEffect(withdrawalState) {
            when (withdrawalState) {
                is WithdrawalState.Success -> snackbarHostState.showSnackbar(
                    "Withdrawal request submitted successfully!"
                )
                is WithdrawalState.Error -> snackbarHostState.showSnackbar(
                    (withdrawalState as WithdrawalState.Error).message
                )
                else -> {}
            }
        }
    }
}
