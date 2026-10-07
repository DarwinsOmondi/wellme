package com.example.wellme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.wellme.presentation.auth.*
import com.example.wellme.presentation.splash.SplashScreen
import com.example.wellme.presentation.merchant.*
import com.example.wellme.presentation.onboarding.*
import com.example.wellme.presentation.profile.*
import com.example.wellme.presentation.student.*
import com.example.wellme.ui.main.MainScreen
import com.example.wellme.util.GuestSession

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Splash> {
                    val viewModel: AuthViewModel = viewModel()
                    SplashScreen(
                        viewModel = viewModel,
                        onNavigateToSignIn = {
                            backStack.clear()
                            backStack.add(SignIn)
                        },
                        onNavigateToStudentMain = {
                            backStack.clear()
                            backStack.add(StudentMain)
                        },
                        onNavigateToMerchantMain = {
                            backStack.clear()
                            backStack.add(MerchantMain)
                        },
                        onNavigateToOnboarding = { email: String, role: String ->
                            backStack.clear()
                            val nextKey = if (role == "STUDENT") {
                                StudentOnboarding(email)
                            } else {
                                MerchantOnboarding(email)
                            }
                            backStack.add(nextKey)
                        }
                    )
                }
                entry<SignIn> {
                    val viewModel: AuthViewModel = viewModel()
                    SignInScreen(
                        viewModel = viewModel,
                        onOtpSent = { email ->
                            backStack.add(Otp(email))
                        },
                        onNavigateToSignUp = { backStack.add(SignUp) },
                        onBrowseAsGuest = {
                            GuestSession.isGuest = true
                            backStack.clear()
                            backStack.add(StudentMain)
                        }
                    )
                }
                entry<SignUp> {
                    val viewModel: AuthViewModel = viewModel()
                    SignUpScreen(
                        viewModel = viewModel,
                        onOtpSent = { email ->
                            backStack.add(Otp(email))
                        },
                        onNavigateToSignIn = { backStack.removeLastOrNull() }
                    )
                }
                entry<Otp> { key ->
                    val viewModel: AuthViewModel = viewModel()
                    androidx.compose.runtime.LaunchedEffect(key.email) {
                        viewModel.email = key.email
                    }
                    OtpScreen(
                        onVerificationSuccess = {
                            val nextKey = if (viewModel.isKycCompleted) {
                                if (viewModel.role == "STUDENT") StudentMain else MerchantMain
                            } else {
                                if (viewModel.role == "STUDENT") {
                                    StudentOnboarding(viewModel.email)
                                } else {
                                    MerchantOnboarding(viewModel.email)
                                }
                            }
                            backStack.add(nextKey)
                        },
                        onNavigateBack = { backStack.removeLastOrNull() },
                        viewModel = viewModel
                    )
                }

                // Student Onboarding
                entry<StudentOnboarding> { key ->
                    val viewModel: OnboardingViewModel = viewModel()
                    androidx.compose.runtime.LaunchedEffect(key.email) {
                        viewModel.campusEmail = key.email
                    }
                    StudentOnboardingScreen(
                        viewModel = viewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        onContinueClick = { backStack.add(StudentVerification) }
                    )
                }
                entry<StudentVerification> {
                    val viewModel: OnboardingViewModel = viewModel()
                    StudentVerificationScreen(
                        viewModel = viewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        onVerifyClick = { backStack.add(StudentMain) },
                        onLaterClick = { backStack.add(StudentMain) }
                    )
                }

                // Merchant Onboarding
                entry<MerchantOnboarding> {
                    val viewModel: OnboardingViewModel = viewModel()
                    MerchantOnboardingScreen(
                        viewModel = viewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        onContinueClick = { backStack.add(LegalDocumentation) }
                    )
                }
                entry<LegalDocumentation> {
                    val viewModel: OnboardingViewModel = viewModel()
                    LegalDocumentationScreen(
                        viewModel = viewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        onSaveDraft = { /* Handle save draft if needed */ },
                        onContinue = { backStack.add(FinancialSettlement) }
                    )
                }
                entry<FinancialSettlement> {
                    val viewModel: OnboardingViewModel = viewModel()
                    FinancialSettlementScreen(
                        viewModel = viewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        onComplete = { backStack.add(MerchantMain) }
                    )
                }

                entry<StudentMain> {
                    val viewModel: StudentViewModel = viewModel()
                    StudentScreen(
                        viewModel = viewModel,
                        onProfileClick = { backStack.add(Profile) },
                        onDiscoverClick = { backStack.add(StudentDiscover) },
                        onNotificationClick = { backStack.add(Notifications) }
                    )
                }

                entry<Notifications> {
                    com.example.wellme.presentation.notifications.NotificationsScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<StudentDiscover> {
                    val viewModel: StudentDiscoverViewModel = hiltViewModel()
                    StudentDiscoverScreen(
                        viewModel = viewModel,
                        onMerchantClick = { id ->
                            viewModel.selectMerchant(id)
                            backStack.add(StudentMerchantDetail(id))
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<StudentMerchantDetail> { key ->
                    val viewModel: StudentDiscoverViewModel = hiltViewModel()
                    androidx.compose.runtime.LaunchedEffect(key.merchantId) {
                        viewModel.selectMerchant(key.merchantId)
                    }
                    StudentMerchantDetailScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<MerchantMain> {
                    val viewModel: MerchantViewModel = viewModel()
                    MerchantScreen(
                        viewModel = viewModel,
                        onProfileClick = { backStack.add(Profile) },
                        onNavigateToAddItem = { backStack.add(AddInventoryItem) },
                        onNavigateToLoansHistory = { backStack.add(MerchantLoans) }
                    )
                }

                entry<MerchantLoans> {
                    val viewModel: MerchantViewModel = viewModel()
                    val loans by viewModel.disbursedLoans.collectAsState()
                    val totalAmount by viewModel.totalLoanAmountKsh.collectAsState()

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        viewModel.loadDisbursedLoans()
                    }

                    MerchantLoansScreen(
                        merchantLoans = loans,
                        totalLoanAmountKsh = totalAmount,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<AddInventoryItem> {
                    val viewModel: MerchantInventoryViewModel = hiltViewModel()
                    AddInventoryItemScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<Profile> {
                    val viewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = viewModel,
                        onSignOut = {
                            GuestSession.isGuest = false
                            backStack.clear()
                            backStack.add(SignIn)
                        },
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToDetail = { type ->
                            val key = when(type) {
                                "personal" -> PersonalDetails
                                "security" -> SecuritySettings
                                "notifications" -> NotificationPreferences
                                "help" -> HelpSupport
                                else -> null
                            }
                            key?.let { backStack.add(it) }
                        },
                        onNavigateToSignIn = {
                            GuestSession.isGuest = false
                            backStack.clear()
                            backStack.add(SignIn)
                        }
                    )
                }

                entry<PersonalDetails> {
                    val viewModel: ProfileViewModel = viewModel()
                    PersonalDetailsScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<SecuritySettings> {
                    SecuritySettingsScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<NotificationPreferences> {
                    NotificationPreferencesScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<HelpSupport> {
                    HelpSupportScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<Main> {
                    // This could be a role dispatcher or a simple landing
                    MainScreen(
                        onItemClick = { navKey -> backStack.add(navKey) },
                        modifier = Modifier
                            .safeDrawingPadding()
                            .padding(16.dp)
                    )
                }
            },
    )
}
