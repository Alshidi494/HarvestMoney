package com.harvestmoney.bounty.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.authState.collectAsState()
    val snackbarHostState = rememberSnackbarHostState()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isFormValid = email.isNotBlank() && password.isNotBlank()
    val isLoading = uiState is AuthState.Loading

    // Handle auth state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            AuthState.Success.SignUp -> onSignUpSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((uiState as AuthState.Error).message)
            }
            else -> {}
        }
    }

    AuthScreenScaffold(
        snackbarHostState = snackbarHostState,
        isLoading = isLoading,
        errorMessage = (uiState as? AuthState.Error)?.message,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmailField(
                    value = email,
                    onValueChange = { email = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    isVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SignUpButton(
                    enabled = isFormValid && !isLoading,
                    onClick = { scope.launch { viewModel.signUp(email, password) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SignInNavigationButton(
                    enabled = !isLoading,
                    onClick = onNavigateToSignIn
                )
            }
        }
    )
}

@Composable
private fun SignUpButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign Up")
    }
}

@Composable
private fun SignInNavigationButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Already have an account? Sign In")
    }
}
