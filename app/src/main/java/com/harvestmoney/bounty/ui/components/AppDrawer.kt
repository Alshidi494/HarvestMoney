package com.harvestmoney.bounty.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.harvestmoney.bounty.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppDrawer(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onSignOut: () -> Unit,
    closeDrawer: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser

    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            user?.email?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = false,
            onClick = {
                onNavigateToHome()
                closeDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("Withdrawal History") },
            selected = false,
            onClick = {
                onNavigateToHistory()
                closeDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = false,
            onClick = {
                onNavigateToProfile()
                closeDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Help, contentDescription = null) },
            label = { Text("FAQ") },
            selected = false,
            onClick = {
                onNavigateToFAQ()
                closeDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Support, contentDescription = null) },
            label = { Text("Support") },
            selected = false,
            onClick = {
                onNavigateToSupport()
                closeDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            label = { Text("Privacy Policy") },
            selected = false,
            onClick = {
                onNavigateToPrivacyPolicy()
                closeDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        
        Divider()
        
        // Sign Out Button
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            label = { Text("Sign Out") },
            selected = false,
            onClick = {
                onSignOut()
                closeDrawer()
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
