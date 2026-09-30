package com.example.wellme.domain.repository

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.OtpType

interface AuthRepository {
    suspend fun signUp(email: String, role: String): Result<Unit>
    suspend fun signIn(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): UserInfo?
    suspend fun signUpWithOtp(email: String): Result<Unit>
    suspend fun verifyOtp(email: String, token: String, type: OtpType.Email): Result<Unit>
    suspend fun resendOtp(email: String, type: OtpType.Email): Result<Unit>
    suspend fun updateUser(password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
}
