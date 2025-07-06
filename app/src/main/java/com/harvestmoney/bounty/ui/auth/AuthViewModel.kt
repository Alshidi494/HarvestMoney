package com.harvestmoney.bounty.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    suspend fun signIn(email: String, password: String) {
        try {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.Success
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Authentication failed")
        }
    }

    suspend fun signUp(email: String, password: String) {
        try {
            // Validate email and password
            if (!isValidEmail(email)) {
                _authState.value = AuthState.Error("Invalid email format")
                return
            }
            if (!isValidPassword(password)) {
                _authState.value = AuthState.Error("Password must be at least 6 characters long")
                return
            }

            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password).await()
            // Create initial user record in Realtime Database
            createUserRecord(auth.currentUser?.uid ?: return, email)
            _authState.value = AuthState.Success
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Registration failed")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    suspend fun resetPassword(email: String) {
        try {
            _authState.value = AuthState.Loading
            auth.sendPasswordResetEmail(email).await()
            _authState.value = AuthState.Success
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Password reset failed")
        }
    }

    private suspend fun createUserRecord(userId: String, email: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("users").child(userId)
        
        val userData = mapOf(
            "email" to email,
            "points" to 0,
            "binance" to "",
            "payeer" to ""
        )
        
        userRef.setValue(userData).await()
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
