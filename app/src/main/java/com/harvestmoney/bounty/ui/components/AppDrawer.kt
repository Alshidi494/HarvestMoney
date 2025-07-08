package com.harvestmoney.bounty.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        
        // Header with user info
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
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

        // Navigation Items with ripple and padding
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = {
                onNavigateToHome()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Withdrawal History") },
            label = { Text("Withdrawal History") },
            selected = false,
            onClick = {
                onNavigateToHistory()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = {
                onNavigateToProfile()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Help, contentDescription = "FAQ") },
            label = { Text("FAQ") },
            selected = false,
            onClick = {
                onNavigateToFAQ()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Support, contentDescription = "Support") },
            label = { Text("Support") },
            selected = false,
            onClick = {
                onNavigateToSupport()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = "Privacy Policy") },
            label = { Text("Privacy Policy") },
            selected = false,
            onClick = {
                onNavigateToPrivacyPolicy()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))
        
        Divider()
        
        // Sign Out Button at bottom
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Logout, contentDescription = "Sign Out") },
            label = { Text("Sign Out") },
            selected = false,
            onClick = {
                onSignOut()
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
