package com.harvestmoney.bounty.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Privacy Policy for Harvest Money",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last updated: July 6, 2025",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            PolicySection(
                title = "1. Information We Collect",
                content = """
                    We collect the following information:
                    • Email address (for authentication)
                    • Points earned through watching ads
                    • Withdrawal history
                    • Payment account information (Binance/Payeer IDs)
                    • Device information for ads delivery
                """.trimIndent()
            )

            PolicySection(
                title = "2. How We Use Your Information",
                content = """
                    Your information is used for:
                    • Account management
                    • Points tracking
                    • Processing withdrawals
                    • Delivering targeted ads
                    • Preventing fraud
                    • Improving user experience
                """.trimIndent()
            )

            PolicySection(
                title = "3. Advertisement Partners",
                content = """
                    We use Google AdMob for advertisements. This involves:
                    • Showing rewarded video ads
                    • Display banner ads
                    • Interstitial ads
                    • App open ads
                    
                    AdMob may collect device information and usage data for targeted advertising.
                """.trimIndent()
            )

            PolicySection(
                title = "4. Data Security",
                content = """
                    We implement security measures including:
                    • Firebase Authentication for secure login
                    • Encrypted data transmission
                    • Secure database storage
                    • Regular security updates
                """.trimIndent()
            )

            PolicySection(
                title = "5. Withdrawal Processing",
                content = """
                    For withdrawals, we:
                    • Verify account ownership
                    • Process payments through secure channels
                    • Maintain withdrawal history
                    • Implement fraud prevention measures
                """.trimIndent()
            )

            PolicySection(
                title = "6. User Rights",
                content = """
                    You have the right to:
                    • Access your personal data
                    • Request data deletion
                    • Update your information
                    • Opt-out of targeted advertising
                    • Delete your account
                """.trimIndent()
            )

            PolicySection(
                title = "7. Children's Privacy",
                content = """
                    Our app is not intended for children under 13. We do not knowingly collect information from children under 13 years old.
                """.trimIndent()
            )

            PolicySection(
                title = "8. Changes to Privacy Policy",
                content = """
                    We may update this privacy policy. Users will be notified of any significant changes through the app or email.
                """.trimIndent()
            )

            PolicySection(
                title = "9. Contact Us",
                content = """
                    For privacy-related questions, contact us at:
                    devv01.supp@gmail.com
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(24.dp))
}
