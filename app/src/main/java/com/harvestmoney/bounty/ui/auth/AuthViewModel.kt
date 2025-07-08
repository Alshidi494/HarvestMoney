// AuthViewModel.kt
package com.harvestmoney.bounty.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val database: FirebaseDatabase = Firebase.database
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                validateCredentials(email, password)
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success.SignIn
            } catch (e: Exception) {
                _authState.value = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> AuthState.Error.InvalidCredentials
                    else -> AuthState.Error.Generic(e.message ?: "Authentication failed")
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                validateCredentials(email, password)
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                authResult.user?.uid?.let { userId ->
                    createUserRecord(userId, email)
                }
                _authState.value = AuthState.Success.SignUp
            } catch (e: Exception) {
                _authState.value = when (e) {
                    is FirebaseAuthWeakPasswordException -> AuthState.Error.WeakPassword
                    is FirebaseAuthUserCollisionException -> AuthState.Error.UserExists
                    else -> AuthState.Error.Generic(e.message ?: "Registration failed")
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (!isValidEmail(email)) {
                    _authState.value = AuthState.Error.InvalidEmail
                    return@launch
                }
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success.PasswordReset
            } catch (e: Exception) {
                _authState.value = AuthState.Error.Generic(e.message ?: "Password reset failed")
            }
        }
    }

    private suspend fun createUserRecord(userId: String, email: String) {
        database.reference.child("users").child(userId).setValue(
            mapOf(
                "email" to email,
                "points" to 0,
                "binance" to "",
                "payeer" to "",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    private fun validateCredentials(email: String, password: String) {
        if (!isValidEmail(email)) throw IllegalArgumentException("Invalid email format")
        if (!isValidPassword(password)) throw IllegalArgumentException("Password too weak")
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Boolean =
        password.length >= 8

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()

        sealed class Success : AuthState() {
            object SignIn : Success()
            object SignUp : Success()
            object PasswordReset : Success()
        }

        sealed class Error : AuthState() {
            object InvalidEmail : Error()
            object InvalidCredentials : Error()
            object WeakPassword : Error()
            object UserExists : Error()
            data class Generic(val message: String) : Error()
        }
    }
}

// SignInScreen.kt
package com.harvestmoney.bounty.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { scope.launch { viewModel.signIn(email, password) } },
                enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthViewModel.AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToSignUp,
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) { Text("Don't have an account? Sign Up") }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { scope.launch { viewModel.resetPassword(email) } },
                enabled = email.isNotBlank() && authState !is AuthViewModel.AuthState.Loading
            ) { Text("Forgot Password?") }

            Spacer(modifier = Modifier.height(16.dp))

            when (authState) {
                is AuthViewModel.AuthState.Loading -> CircularProgressIndicator()
                is AuthViewModel.AuthState.Error -> Text(
                    text = (authState as AuthViewModel.AuthState.Error).
                        let { if (it is AuthViewModel.AuthState.Error.Generic) it.message else it.toString() },
                    color = MaterialTheme.colorScheme.error
                )
                AuthViewModel.AuthState.Success.SignIn -> LaunchedEffect(Unit) { onSignInSuccess() }
                AuthViewModel.AuthState.Success.PasswordReset -> LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Password reset email sent.")
                }
                else -> Unit
            }
        }
    }
}
