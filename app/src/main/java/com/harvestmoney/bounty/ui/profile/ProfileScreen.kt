package com.harvestmoney.bounty.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.harvestmoney.bounty.ui.home.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val authUser = FirebaseAuth.getInstance().currentUser
    val profile by viewModel.profile.collectAsState()

    var binanceId by remember { mutableStateOf("") }
    var payeerId by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Load once
    LaunchedEffect(Unit) {
        binanceId = profile.defaultMethod.takeIf { it == "Binance" }?.let { profile.account } ?: ""
        payeerId = profile.defaultMethod.takeIf { it == "Payeer" }?.let { profile.account } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Email", style = MaterialTheme.typography.titleMedium)
            Text(authUser?.email ?: "", style = MaterialTheme.typography.bodyLarge)

            Divider()

            Text("Payment Methods", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = binanceId,
                onValueChange = { if (isEditing) binanceId = it },
                label = { Text("Binance ID") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = payeerId,
                onValueChange = { if (isEditing) payeerId = it },
                label = { Text("Payeer ID") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            if (isEditing) {
                Button(
                    onClick = {
                        val uid = authUser?.uid ?: return@Button
                        val database = FirebaseDatabase.getInstance()
                        val updates = mapOf(
                            "binance" to binanceId,
                            "payeer" to payeerId
                        )
                        scope.launch {
                            database.reference.child("users").child(uid)
                                .updateChildren(updates)
                                .addOnSuccessListener {
                                    isEditing = false
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
