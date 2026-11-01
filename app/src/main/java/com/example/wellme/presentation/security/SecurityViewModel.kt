package com.example.wellme.presentation.security

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.data.local.SecurityDao
import com.example.wellme.data.local.SecuritySettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityDao: SecurityDao
) : ViewModel() {
    private val TAG = "SecurityViewModel"

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isWalletLocked = MutableStateFlow(true)
    val isWalletLocked: StateFlow<Boolean> = _isWalletLocked.asStateFlow()

    private val _isSensitiveDataLocked = MutableStateFlow(true)
    val isSensitiveDataLocked: StateFlow<Boolean> = _isSensitiveDataLocked.asStateFlow()

    init {
        viewModelScope.launch {
            securityDao.getSecuritySettingsFlow().collect { settings ->
                val enabled = settings?.isBiometricEnabled ?: true
                _isBiometricEnabled.value = enabled
                if (!enabled) {
                    _isWalletLocked.value = false
                    _isSensitiveDataLocked.value = false
                }
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        Log.d(TAG, "setBiometricEnabled: $enabled")
        _isBiometricEnabled.value = enabled
        if (!enabled) {
            _isWalletLocked.value = false
            _isSensitiveDataLocked.value = false
        }
        viewModelScope.launch {
            securityDao.insertSecuritySettings(SecuritySettingsEntity(id = 1, isBiometricEnabled = enabled))
        }
    }

    fun unlockWallet() {
        Log.d(TAG, "unlockWallet()")
        _isWalletLocked.value = false
    }

    fun lockWallet() {
        Log.d(TAG, "lockWallet()")
        if (_isBiometricEnabled.value) {
            _isWalletLocked.value = true
        }
    }

    fun unlockSensitiveData() {
        Log.d(TAG, "unlockSensitiveData()")
        _isSensitiveDataLocked.value = false
    }

    fun lockSensitiveData() {
        Log.d(TAG, "lockSensitiveData()")
        if (_isBiometricEnabled.value) {
            _isSensitiveDataLocked.value = true
        }
    }
}
