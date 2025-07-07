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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
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
            AuthState.SignInSuccess -> onSignInSuccess()
            is AuthState.ResetSuccess -> {
                snackbarHostState.showSnackbar("Password reset email sent.")
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

                SignInButton(
                    enabled = isFormValid && !isLoading,
                    onClick = { scope.launch { viewModel.signIn(email, password) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SignUpNavigationButton(
                    enabled = !isLoading,
                    onClick = onNavigateToSignUp
                )

                Spacer(modifier = Modifier.height(8.dp))

                ForgotPasswordButton(
                    enabled = email.isNotBlank() && !isLoading,
                    onClick = { scope.launch { viewModel.resetPassword(email) } }
                )
            }
        }
    )
}

@Composable
private fun AuthScreenScaffold(
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean,
    errorMessage: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box {
            content(paddingValues)

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrect = false,
            keyboardType = KeyboardType.Email
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None 
                            else PasswordVisualTransformation(),
        trailingIcon = {
            PasswordVisibilityToggle(
                isVisible = isVisible,
                onToggle = { onVisibilityChange(!isVisible) }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordVisibilityToggle(
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isVisible) Icons.Default.Visibility 
                         else Icons.Default.VisibilityOff,
            contentDescription = if (isVisible) "Hide password" 
                              else "Show password"
        )
    }
}

@Composable
private fun SignInButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign In")
    }
}

@Composable
private fun SignUpNavigationButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Don't have an account? Sign Up")
    }
}

@Composable
private fun ForgotPasswordButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Forgot Password?")
    }
}
