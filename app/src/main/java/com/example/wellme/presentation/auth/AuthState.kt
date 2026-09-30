package com.example.wellme.presentation.auth

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data object Success : AuthState()
    data object OtpSent : AuthState()
}

enum class UserRole {
    STUDENT, MERCHANT
}
