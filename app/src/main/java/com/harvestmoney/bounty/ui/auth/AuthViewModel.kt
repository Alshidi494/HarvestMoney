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
                    is FirebaseAuthInvalidCredentialsException -> 
                        AuthState.Error.InvalidCredentials
                    else -> 
                        AuthState.Error.Generic(e.message ?: "Authentication failed")
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
                    is FirebaseAuthWeakPasswordException -> 
                        AuthState.Error.WeakPassword
                    is FirebaseAuthUserCollisionException -> 
                        AuthState.Error.UserExists
                    else -> 
                        AuthState.Error.Generic(e.message ?: "Registration failed")
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
                _authState.value = AuthState.Error.Generic(
                    e.message ?: "Password reset failed"
                )
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

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
