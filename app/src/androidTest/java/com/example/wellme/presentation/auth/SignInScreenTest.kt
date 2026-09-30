package com.example.wellme.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.wellme.R
import com.example.wellme.theme.WellMeTheme
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun signInScreen_elementsAreDisplayed() {
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "",
                    onEmailChange = {},
                    isLoading = false,
                    onSignInClick = {},
                    onNavigateToSignUp = {},
                )
            }
        }

        val welcomeText = composeTestRule.activity.getString(R.string.welcome_back)
        val emailLabel = composeTestRule.activity.getString(R.string.campus_or_business_email)
        val buttonText = "Continue with OTP"

        composeTestRule.onNodeWithText(welcomeText).assertExists()
        composeTestRule.onNodeWithText(emailLabel).assertExists()
        composeTestRule.onNodeWithText(buttonText).assertExists()
    }

    @Test
    fun signInScreen_typingUpdatesEmail() {
        var emailValue = ""
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = emailValue,
                    onEmailChange = { emailValue = it },
                    isLoading = false,
                    onSignInClick = {},
                    onNavigateToSignUp = {},
                )
            }
        }

        val emailHint = composeTestRule.activity.getString(R.string.email_hint)
        composeTestRule.onNodeWithText(emailHint).performTextInput("test@wellme.com")
        
        assert(emailValue == "test@wellme.com")
    }

    @Test
    fun signInScreen_buttonDisabledWhenEmailIsBlank() {
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "",
                    onEmailChange = {},
                    isLoading = false,
                    onSignInClick = {},
                    onNavigateToSignUp = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Continue with OTP").assertIsNotEnabled()
    }

    @Test
    fun signInScreen_buttonEnabledWhenEmailIsNotBlank() {
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "user@wellme.com",
                    onEmailChange = {},
                    isLoading = false,
                    onSignInClick = {},
                    onNavigateToSignUp = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Continue with OTP").assertIsEnabled()
    }

    @Test
    fun signInScreen_loadingShowsProgress() {
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "user@wellme.com",
                    onEmailChange = {},
                    isLoading = true,
                    onSignInClick = {},
                    onNavigateToSignUp = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sending OTP...").assertExists()
        composeTestRule.onNodeWithText("Sending OTP...").assertIsNotEnabled()
    }

    @Test
    fun signInScreen_signInClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "user@wellme.com",
                    onEmailChange = {},
                    isLoading = false,
                    onSignInClick = { clicked = true },
                    onNavigateToSignUp = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Continue with OTP").performClick()
        assert(clicked)
    }

    @Test
    fun signInScreen_navigateToSignUpTriggersCallback() {
        var navigated = false
        composeTestRule.setContent {
            WellMeTheme {
                SignInScreenContent(
                    email = "",
                    onEmailChange = {},
                    isLoading = false,
                    onSignInClick = {},
                    onNavigateToSignUp = { navigated = true },
                )
            }
        }

        val signUpAction = composeTestRule.activity.getString(R.string.sign_up_action)
        composeTestRule.onNodeWithText(signUpAction, substring = true).performClick()
        assert(navigated)
    }
}
