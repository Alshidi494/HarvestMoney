package com.harvestmoney.bounty.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

    // State variables
    val points by viewModel.points.collectAsState()
    val withdrawalState by viewModel.withdrawalState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val withdrawals by viewModel.withdrawalHistory.collectAsState()

    // Local states
    var selectedMethod by remember { mutableStateOf(profile.defaultMethod) }
    var accountDetails by remember { mutableStateOf(profile.account) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

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
            // Points Display
            Text(
                text = "Current Points: $points",
                style = MaterialTheme.typography.headlineMedium
            )

            // Watch Ad Button
            Button(
                onClick = { viewModel.showRewardedAd() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Watch Ad (+${viewModel.rewardAmount} points)")
            }

            // Withdraw Button
            Button(
                onClick = { showWithdrawalDialog = true },
                enabled = points >= viewModel.minWithdrawalPoints,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Withdraw Points (${viewModel.minWithdrawalPoints} points = $1)")
            }

            // Interstitial Ad Trigger
            LaunchedEffect(points) {
                if (points > 0 && points % viewModel.interstitialInterval == 0) {
                    viewModel.showInterstitialAd()
                }
            }

            // Withdrawal History Section
            Text(
                text = "Withdrawal History",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(withdrawals) { withdrawal ->
                    WithdrawalItemCard(
                        withdrawal = withdrawal,
                        minWithdrawalPoints = viewModel.minWithdrawalPoints
                    )
                }
            }

            // Banner Ad
            AdBannerView()
        }

        // Withdrawal Dialog
        if (showWithdrawalDialog) {
            WithdrawalDialog(
                selectedMethod = selectedMethod,
                accountDetails = accountDetails,
                onMethodSelected = { selectedMethod = it },
                onAccountDetailsChanged = { accountDetails = it },
                onConfirm = {
                    viewModel.requestWithdrawal(selectedMethod, accountDetails)
                    showWithdrawalDialog = false
                },
                onDismiss = { showWithdrawalDialog = false },
                dropdownExpanded = dropdownExpanded,
                onDropdownExpandedChange = { dropdownExpanded = it }
            )
        }

        // Withdrawal State Snackbar
        WithdrawalStateSnackbar(
            withdrawalState = withdrawalState,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
private fun WithdrawalItemCard(
    withdrawal: Withdrawal,
    minWithdrawalPoints: Int
) {
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
                Text("Amount: \$${withdrawal.amount / minWithdrawalPoints}")
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
                text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(withdrawal.timestamp))}"
            )
        }
    }
}

@Composable
private fun AdBannerView() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-7816293804229825/3035899826"
            }
        },
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}

@Composable
private fun WithdrawalDialog(
    selectedMethod: String,
    accountDetails: String,
    onMethodSelected: (String) -> Unit,
    onAccountDetailsChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Points") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Method Selection Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        label = { Text("Withdrawal Method") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        onClick = { onDropdownExpandedChange(true) }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { onDropdownExpandedChange(false) }
                    ) {
                        listOf("Binance", "Payeer").forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    onMethodSelected(method)
                                    onDropdownExpandedChange(false)
                                }
                            )
                        }
                    }
                }

                // Account Details Input
                OutlinedTextField(
                    value = accountDetails,
                    onValueChange = onAccountDetailsChanged,
                    label = { Text("Account Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedMethod.isNotEmpty() && accountDetails.isNotEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WithdrawalStateSnackbar(
    withdrawalState: WithdrawalState,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(withdrawalState) {
        when (withdrawalState) {
            is WithdrawalState.Success -> {
                snackbarHostState.showSnackbar(
                    "Withdrawal request submitted successfully!"
                )
            }
            is WithdrawalState.Error -> {
                snackbarHostState.showSnackbar(
                    withdrawalState.message
                )
            }
            else -> {}
        }
    }
}
