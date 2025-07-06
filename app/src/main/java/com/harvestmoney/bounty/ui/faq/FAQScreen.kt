package com.harvestmoney.bounty.ui.faq

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onNavigateBack: () -> Unit
) {
    val faqs = listOf(
        FAQ(
            question = "How do I earn points?",
            answer = "You can earn points by watching rewarded video ads. Each completed ad view earns you 5 points."
        ),
        FAQ(
            question = "How many points do I need to withdraw?",
            answer = "You need a minimum of 1000 points to make a withdrawal. 1000 points = \$1."
        ),
        FAQ(
            question = "What payment methods are available?",
            answer = "We currently support withdrawals through Binance and Payeer. Make sure to add your payment IDs in your profile."
        ),
        FAQ(
            question = "How long does it take to process withdrawals?",
            answer = "Withdrawals are typically processed within 24-48 hours after request submission."
        ),
        FAQ(
            question = "Why was my withdrawal rejected?",
            answer = "Withdrawals may be rejected if the payment ID is invalid or if we detect any suspicious activity. Please ensure your payment information is correct."
        ),
        FAQ(
            question = "Can I have multiple accounts?",
            answer = "No, multiple accounts are not allowed. Any detected violation may result in account suspension."
        ),
        FAQ(
            question = "How do I reset my password?",
            answer = "You can use the 'Forgot Password' option on the sign-in screen to receive a password reset email."
        ),
        FAQ(
            question = "Is the app available worldwide?",
            answer = "Yes, the app is available worldwide. However, ad availability may vary by region."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(faqs) { faq ->
                FAQItem(faq)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FAQItem(faq: FAQ) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

data class FAQ(
    val question: String,
    val answer: String
)
