package com.example.wellme.data.repository

import android.util.Log
import com.codeskop.sdk.core.Codeskop
import com.example.wellme.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth
) : AuthRepository {

    private val TAG = "AuthRepositoryImpl"

    override suspend fun signUp(email: String, role: String): Result<Unit> {
        Log.d(TAG, "Attempting signUp for: $email with role: $role")
        return try {
            auth.signUpWith(OTP) {
                this.email = email
                data = buildJsonObject {
                    put("role", role)
                }
            }
            Log.d(TAG, "signUp successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signUp failed for: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String): Result<Unit> {
        Log.d(TAG, "Attempting signIn for: $email")
        return try {
            auth.signInWith(OTP) {
                this.email = email
            }
            Log.d(TAG, "signIn OTP sent for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signIn failed for: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        Log.d(TAG, "Attempting signOut")
        return try {
            auth.signOut()
            Codeskop.reset()
            Log.d(TAG, "signOut successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signOut failed", e)
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): UserInfo? {
        val user = auth.currentUserOrNull()
        user?.let { u ->
            Codeskop.identify(
                userId = u.id,
                traits = mapOf("role" to u.appMetadata?.get("role"))
            )
        }
        Log.d(TAG, "getCurrentUser: ${user?.id ?: "No user logged in"}")
        return user
    }

    override suspend fun signUpWithOtp(email: String): Result<Unit> {
        Log.d(TAG, "Attempting signUpWithOtp for: $email")
        return try {
            auth.signInWith(OTP) {
                this.email = email
            }
            Log.d(TAG, "signUpWithOtp request successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithOtp failed for: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(
        email: String,
        token: String,
        type: OtpType.Email
    ): Result<Unit> {
        Log.d(TAG, "Attempting verifyOtp for: $email, type: $type")
        return try {
            auth.verifyEmailOtp(type, email, token)
            Log.d(TAG, "verifyOtp successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp failed for: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun resendOtp(email: String, type: OtpType.Email): Result<Unit> {
        Log.d(TAG, "Attempting resendOtp for: $email, type: $type")
        return try {
            auth.resendEmail(type, email)
            Log.d(TAG, "resendOtp successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "resendOtp failed for: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUser(password: String): Result<Unit> {
        Log.d(TAG, "Attempting updateUser password")
        return try {
            auth.updateUser {
                this.password = password
            }
            Log.d(TAG, "updateUser successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateUser failed", e)
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        Log.d(TAG, "Attempting resetPassword for: $email")
        return try {
            auth.resetPasswordForEmail(email)
            Log.d(TAG, "resetPassword request successful for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "resetPassword failed for: $email", e)
            Result.failure(e)
        }
    }
}
