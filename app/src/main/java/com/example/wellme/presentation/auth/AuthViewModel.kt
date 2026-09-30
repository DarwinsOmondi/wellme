package com.example.wellme.presentation.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val kycRepository: KycRepository
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()

    // Sign In/Up States
    var email by mutableStateOf("")
    var role by mutableStateOf("STUDENT")
    var otp by mutableStateOf("")
    var isKycCompleted by mutableStateOf(false)
        private set

    fun onEmailChange(value: String) { email = value }
    fun onRoleChange(value: String) { role = value }
    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            otp = value
        }
    }

    fun clearError() {
        if (_state.value is AuthState.Error) {
            Log.d(TAG, "clearing auth error")
            _state.value = AuthState.Idle
        }
    }

    fun signIn() {
        Log.d(TAG, "signIn attempt for $email")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.signIn(email)
                .onSuccess {
                    Log.d(TAG, "signIn OTP sent for $email")
                    _state.value = AuthState.OtpSent 
                }
                .onFailure { 
                    Log.e(TAG, "signIn failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }

    fun signUp() {
        Log.d(TAG, "signUp attempt for $email with role $role")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.signUp(email, role)
                .onSuccess { 
                    Log.d(TAG, "signUp success for $email, OTP sent")
                    _state.value = AuthState.OtpSent
                }
                .onFailure { 
                    Log.e(TAG, "signUp failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }

    fun signUpWithOtp(email: String) {
        Log.d(TAG, "signUpWithOtp attempt for $email")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.signUpWithOtp(email)
                .onSuccess { 
                    Log.d(TAG, "signUpWithOtp success, OTP sent to $email")
                    _state.value = AuthState.OtpSent 
                }
                .onFailure { 
                    Log.e(TAG, "signUpWithOtp failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }

    fun getCurrentUser() = repository.getCurrentUser()

    fun checkKycAndRoute(
        userId: String,
        email: String,
        onStudentMain: () -> Unit,
        onMerchantMain: () -> Unit,
        onOnboarding: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = repository.getCurrentUser()
            val userRole = user?.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "STUDENT"
            role = userRole

            val studentKyc = kycRepository.getStudentKyc(userId).getOrNull()
            val merchantKyc = kycRepository.getMerchantKyc(userId).getOrNull()

            if (studentKyc != null || merchantKyc != null) {
                if (role == "STUDENT") onStudentMain() else onMerchantMain()
            } else {
                onOnboarding(email, role)
            }
        }
    }

    fun verifyOtp(email: String, token: String) {
        Log.d(TAG, "verifyOtp attempt for $email")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.verifyOtp(email, token, OtpType.Email.EMAIL)
                .onSuccess { 
                    Log.d(TAG, "verifyOtp success for $email")
                    val user = repository.getCurrentUser()
                    val userId = user?.id ?: ""
                    val userRole = user?.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "STUDENT"
                    role = userRole
                    
                    // Check if KYC is completed to decide routing
                    val studentKyc = kycRepository.getStudentKyc(userId).getOrNull()
                    val merchantKyc = kycRepository.getMerchantKyc(userId).getOrNull()
                    isKycCompleted = studentKyc != null || merchantKyc != null
                    
                    Log.d(TAG, "KYC Status: $isKycCompleted for role: $role")
                    _state.value = AuthState.Success 
                }
                .onFailure { 
                    Log.e(TAG, "verifyOtp failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }

    fun resendOtp(email: String) {
        Log.d(TAG, "resendOtp attempt for $email")
        viewModelScope.launch {
            repository.resendOtp(email, OtpType.Email.EMAIL)
                .onSuccess {
                    Log.d(TAG, "resendOtp success for $email")
                }
                .onFailure { 
                    Log.e(TAG, "resendOtp failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }

    fun resetPassword(email: String) {
        Log.d(TAG, "resetPassword attempt for $email")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.resetPassword(email)
                .onSuccess { 
                    Log.d(TAG, "resetPassword success, reset link sent to $email")
                    _state.value = AuthState.OtpSent 
                }
                .onFailure { 
                    Log.e(TAG, "resetPassword failed for $email", it)
                    _state.value = AuthState.Error(ErrorMapper.getUserFriendlyMessage(it)) 
                }
        }
    }
}
