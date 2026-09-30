package com.example.wellme.presentation.profile

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.wellme.data.remote.model.MerchantKyc
import com.example.wellme.data.remote.model.StudentKyc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val fullName: String,
    val userId: String,
    val avatarUrl: String?,
    val studentKyc: StudentKyc? = null,
    val merchantKyc: MerchantKyc? = null
)

sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    object SignedOut : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kycRepository: KycRepository
) : ViewModel() {

    private val TAG = "ProfileViewModel"

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = authRepository.getCurrentUser() ?: return
        Log.d(TAG, "Loading profile for user: ${user.id}")
        
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            
            // Force a slight delay or just bypass cache if possible, but re-fetching is key
            val studentResult = kycRepository.getStudentKyc(user.id)
            studentResult.onSuccess { studentKyc ->
                if (studentKyc != null) {
                    Log.d(TAG, "Student Profile Loaded. Avatar URL: ${studentKyc.avatarUrl}")
                    _state.value = ProfileUiState.Success(
                        UserProfile(
                            fullName = studentKyc.studentName,
                            userId = "STUDENT ID: ${studentKyc.studentIdNumber}",
                            avatarUrl = studentKyc.avatarUrl ?: studentKyc.studentIdPhotoUrl,
                            studentKyc = studentKyc
                        )
                    )
                    return@launch
                }
            }

            val merchantResult = kycRepository.getMerchantKyc(user.id)
            merchantResult.onSuccess { merchantKyc ->
                if (merchantKyc != null) {
                    Log.d(TAG, "Merchant Profile Loaded. Avatar URL: ${merchantKyc.avatarUrl}")
                    _state.value = ProfileUiState.Success(
                        UserProfile(
                            fullName = merchantKyc.businessName,
                            userId = "MERCHANT ID: ${user.id.take(8).uppercase()}",
                            avatarUrl = merchantKyc.avatarUrl,
                            merchantKyc = merchantKyc
                        )
                    )
                    return@launch
                }
            }

            _state.value = ProfileUiState.Error("Profile not found")
        }
    }

    fun updateProfileImage(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            try {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    _state.value = ProfileUiState.Error("Failed to read image")
                    return@launch
                }
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)) ?: "jpg"
                
                kycRepository.updateAvatar(bytes, extension).onSuccess { newUrl ->
                    Log.d(TAG, "Avatar updated successfully: $newUrl")
                    // Add a small delay to ensure Supabase DB is consistent before re-fetch
                    kotlinx.coroutines.delay(1000)
                    loadUserProfile()
                }.onFailure {
                    _state.value = ProfileUiState.Error(ErrorMapper.getUserFriendlyMessage(it))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating avatar", e)
                _state.value = ProfileUiState.Error("Failed to update profile image")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().onSuccess {
                _state.value = ProfileUiState.SignedOut
            }.onFailure {
                _state.value = ProfileUiState.Error("Failed to sign out")
            }
        }
    }
}
