package com.example.wellme

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Splash : NavKey
@Serializable data object Main : NavKey
@Serializable data object StudentMain : NavKey
@Serializable data object MerchantMain : NavKey
@Serializable data object Profile : NavKey
@Serializable data object SignIn : NavKey
@Serializable data object SignUp : NavKey
@Serializable data class Otp(val email: String) : NavKey

// Student Onboarding
@Serializable data class StudentOnboarding(val email: String) : NavKey
@Serializable data object StudentVerification : NavKey

// Merchant Onboarding
@Serializable data class MerchantOnboarding(val email: String) : NavKey
@Serializable data object LegalDocumentation : NavKey
@Serializable data object FinancialSettlement : NavKey

// Profile Details
@Serializable data object PersonalDetails : NavKey
@Serializable data object SecuritySettings : NavKey
@Serializable data object NotificationPreferences : NavKey
@Serializable data object HelpSupport : NavKey

// Merchant Inventory
@Serializable data object AddInventoryItem : NavKey

// Student Discover & Notifications
@Serializable data object StudentDiscover : NavKey
@Serializable data class StudentMerchantDetail(val merchantId: String) : NavKey
@Serializable data object Notifications : NavKey
