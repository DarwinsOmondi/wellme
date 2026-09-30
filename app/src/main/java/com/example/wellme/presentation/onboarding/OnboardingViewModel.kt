package com.example.wellme.presentation.onboarding

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingState {
    object Idle : OnboardingState
    object Loading : OnboardingState
    object Success : OnboardingState
    data class Error(val message: String) : OnboardingState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val kycRepository: KycRepository
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state = _state.asStateFlow()

    // Student States
    var fullName by mutableStateOf("")
    var studentIdNumber by mutableStateOf("")
    var campusEmail by mutableStateOf("")
    var phoneNumber by mutableStateOf("254")

    // Merchant States
    var businessName by mutableStateOf("")
    var nationalIdNumber by mutableStateOf("")
    var tillNumber by mutableStateOf("")

    // Verification & Document States
    var selectedFileUri by mutableStateOf<Uri?>(null)
    var fileName by mutableStateOf("")
    var isUploading by mutableStateOf(false)

    var nationalIdUri by mutableStateOf<Uri?>(null)
    var businessCertUri by mutableStateOf<Uri?>(null)
    var businessPermitUri by mutableStateOf<Uri?>(null)

    fun onFullNameChange(value: String) { fullName = value }
    fun onStudentIdNumberChange(value: String) { studentIdNumber = value }
    fun onCampusEmailChange(value: String) { campusEmail = value }
    fun onPhoneNumberChange(value: String) { phoneNumber = value }
    fun onBusinessNameChange(value: String) { businessName = value }
    fun onNationalIdNumberChange(value: String) { nationalIdNumber = value }
    fun onTillNumberChange(value: String) { tillNumber = value }

    fun onFileSelected(uri: Uri?, name: String) {
        Log.d(TAG, "onFileSelected: $name, uri: $uri")
        selectedFileUri = uri
        fileName = name
        isUploading = uri != null
    }

    fun onDocumentSelected(type: String, uri: Uri?) {
        Log.d(TAG, "onDocumentSelected: $type, uri: $uri")
        when (type) {
            "national_id" -> nationalIdUri = uri
            "business_cert" -> businessCertUri = uri
            "business_permit" -> businessPermitUri = uri
        }
    }

    fun clearSelectedFile() {
        Log.d(TAG, "clearSelectedFile")
        selectedFileUri = null
        fileName = ""
        isUploading = false
    }

    fun clearError() {
        if (_state.value is OnboardingState.Error) {
            Log.d(TAG, "clearing onboarding error")
            _state.value = OnboardingState.Idle
        }
    }

    private fun getBytesFromUri(contentResolver: android.content.ContentResolver, uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading bytes from URI: $uri", e)
            null
        }
    }

    private fun getExtensionFromUri(contentResolver: android.content.ContentResolver, uri: Uri): String {
        return android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)) ?: "jpg"
    }

    fun submitStudentKyc(contentResolver: android.content.ContentResolver) {
        val uri = selectedFileUri ?: return
        Log.d(TAG, "submitStudentKyc for: $fullName, $studentIdNumber")
        val bytes = getBytesFromUri(contentResolver, uri)
        if (bytes == null) {
            _state.value = OnboardingState.Error("Failed to read selected file")
            return
        }
        val extension = getExtensionFromUri(contentResolver, uri)
        uploadStudentKyc(studentIdNumber, fullName, campusEmail, phoneNumber, bytes, extension)
    }

    fun submitMerchantKyc(contentResolver: android.content.ContentResolver) {
        Log.d(TAG, "submitMerchantKyc for: $businessName, $nationalIdNumber")
        
        val nationalIdDoc = nationalIdUri?.let { uri ->
            val bytes = getBytesFromUri(contentResolver, uri)
            val ext = getExtensionFromUri(contentResolver, uri)
            if (bytes != null) bytes to ext else null
        }
        
        val registrationCert = businessCertUri?.let { uri ->
            val bytes = getBytesFromUri(contentResolver, uri)
            val ext = getExtensionFromUri(contentResolver, uri)
            if (bytes != null) bytes to ext else null
        }
        
        val businessPermit = businessPermitUri?.let { uri ->
            val bytes = getBytesFromUri(contentResolver, uri)
            val ext = getExtensionFromUri(contentResolver, uri)
            if (bytes != null) bytes to ext else null
        }

        val mpesaTillDoc = selectedFileUri?.let { uri ->
            val bytes = getBytesFromUri(contentResolver, uri)
            val ext = getExtensionFromUri(contentResolver, uri)
            if (bytes != null) bytes to ext else null
        }

        uploadMerchantKyc(
            businessName,
            nationalIdNumber,
            nationalIdDoc,
            registrationCert,
            businessPermit,
            mpesaTillDoc
        )
    }

    private fun uploadStudentKyc(
        studentIdNumber: String,
        studentName: String,
        campusEmail: String,
        phoneNumber: String,
        idPhotoBytes: ByteArray,
        idPhotoExtension: String
    ) {
        Log.d(TAG, "uploadStudentKyc started")
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            kycRepository.uploadStudentKyc(
                studentIdNumber,
                studentName,
                campusEmail,
                phoneNumber,
                idPhotoBytes,
                idPhotoExtension
            ).onSuccess {
                Log.d(TAG, "uploadStudentKyc success")
                _state.value = OnboardingState.Success
            }.onFailure {
                Log.e(TAG, "uploadStudentKyc failed", it)
                _state.value = OnboardingState.Error(ErrorMapper.getUserFriendlyMessage(it))
            }
        }
    }

    fun uploadMerchantKyc(
        businessName: String,
        nationalIdNumber: String,
        nationalIdDoc: Pair<ByteArray, String>?,
        registrationCert: Pair<ByteArray, String>?,
        businessPermit: Pair<ByteArray, String>?,
        mpesaTillDoc: Pair<ByteArray, String>?
    ) {
        Log.d(TAG, "uploadMerchantKyc started")
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            kycRepository.uploadMerchantKyc(
                businessName,
                nationalIdNumber,
                nationalIdDoc,
                registrationCert,
                businessPermit,
                mpesaTillDoc
            ).onSuccess {
                Log.d(TAG, "uploadMerchantKyc success")
                _state.value = OnboardingState.Success
            }.onFailure {
                Log.e(TAG, "uploadMerchantKyc failed", it)
                _state.value = OnboardingState.Error(ErrorMapper.getUserFriendlyMessage(it))
            }
        }
    }
}
