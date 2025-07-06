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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    var binanceId by remember { mutableStateOf("") }
    var payeerId by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    
    val user = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance()

    // Load payment IDs
    LaunchedEffect(Unit) {
        user?.uid?.let { userId ->
            database.reference.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    binanceId = snapshot.child("binance").getValue(String::class.java) ?: ""
                    payeerId = snapshot.child("payeer").getValue(String::class.java) ?: ""
                }
        }
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
            // Email
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge
            )

            Divider()

            // Payment Methods
            Text(
                text = "Payment Methods",
                style = MaterialTheme.typography.titleMedium
            )

            // Binance ID
            OutlinedTextField(
                value = binanceId,
                onValueChange = { if (isEditing) binanceId = it },
                label = { Text("Binance ID") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            // Payeer ID
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
                        user?.uid?.let { userId ->
                            database.reference.child("users").child(userId)
                                .updateChildren(mapOf(
                                    "binance" to binanceId,
                                    "payeer" to payeerId
                                )).addOnSuccessListener {
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
