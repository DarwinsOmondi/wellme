package com.example.wellme.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.theme.EmeraldGreen
import com.example.wellme.theme.WellMeTheme

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onOtpSent: (String) -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.OtpSent -> onOtpSent(viewModel.email)
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((state as AuthState.Error).message)
                viewModel.clearError()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        SignInScreenContent(
            email = viewModel.email,
            onEmailChange = viewModel::onEmailChange,
            isLoading = state is AuthState.Loading,
            onSignInClick = viewModel::signIn,
            onNavigateToSignUp = onNavigateToSignUp,
            modifier = modifier
        )
    }
}

@Composable
fun SignInScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isButtonEnabled = email.isNotBlank() && !isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAF8),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.size(24.dp)) // For balance
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = stringResource(R.string.welcome_back),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.welcome_back_subtitle),
                fontSize = 16.sp,
                color = Color(0xFF44474E),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.campus_or_business_email),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF44474E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.email_hint),
                        color = Color(0xFF8E9199)
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = null,
                        tint = Color(0xFF44474E),
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FB),
                    unfocusedContainerColor = Color(0xFFF8F9FB),
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = Color.Transparent,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E7D56)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sending OTP...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Continue with OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val signUpText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF44474E))) {
                    append(stringResource(R.string.dont_have_account))
                }
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF006760),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(stringResource(R.string.sign_up_action))
                }
            }

            Text(
                text = signUpText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSignUp() },
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    WellMeTheme {
        SignInScreenContent(
            email = "",
            onEmailChange = {},
            isLoading = false,
            onSignInClick = {},
            onNavigateToSignUp = {}
        )
    }
}
