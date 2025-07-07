// File: ui/splash/SplashScreen.kt
package com.harvestmoney.bounty.ui.splash

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.harvestmoney.bounty.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    var showNoInternet by remember { mutableStateOf(false) }
    var maintenance by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        // 1) Check network
        if (!hasNetwork(context)) {
            showNoInternet = true
            return@LaunchedEffect
        }
        // 2) Fetch maintenance flag
        FirebaseDatabase.getInstance()
            .getReference("Maintenance")
            .get()
            .addOnSuccessListener { snap ->
                maintenance = snap.getValue(Boolean::class.java) ?: false
            }
            .addOnFailureListener {
                maintenance = false
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                showNoInternet -> Unit // dialog handled below
                maintenance == null -> {
                    // Loading logo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher),
                            contentDescription = "Logo",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
                maintenance == true -> MaintenanceScreen(context as Activity)
                maintenance == false -> {
                    // proceed after delay
                    LaunchedEffect(Unit) {
                        delay(2000)
                        val dest = if (FirebaseAuth.getInstance().currentUser != null) "home" else "signIn"
                        navController.navigate(dest) { popUpTo("splash") { inclusive = true } }
                    }
                }
            }
        }

        // No Internet Dialog
        if (showNoInternet) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("No Internet") },
                text = { Text("Please check your connection.") },
                confirmButton = {
                    TextButton(onClick = {
                        showNoInternet = false
                        if (!hasNetwork(context)) showNoInternet = true
                        else maintenance = null
                    }) {
                        Text("Retry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) {
                        Text("Exit")
                    }
                }
            )
        }
    }
}

@Composable
private fun MaintenanceScreen(activity: Activity) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "We'll be back soon!",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "App under maintenance.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { activity.finish() }) {
                Text("Exit")
            }
        }
    }
}

private fun hasNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}
