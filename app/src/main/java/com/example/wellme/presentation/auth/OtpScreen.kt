package com.example.wellme.presentation.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.theme.EmeraldDark
import com.example.wellme.theme.EmeraldGreen
import com.example.wellme.theme.WellMeTheme

@Composable
fun OtpScreen(
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success -> onVerificationSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((state as AuthState.Error).message)
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            OtpScreenTopBar(onBackClick = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        OtpScreenContent(
            email = viewModel.email,
            otp = viewModel.otp,
            onOtpChange = viewModel::onOtpChange,
            state = state,
            onVerifyClick = { viewModel.verifyOtp(viewModel.email, viewModel.otp) },
            onResendClick = { viewModel.resendOtp(viewModel.email) },
            focusRequester = focusRequester,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun OtpScreenContent(
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    state: AuthState,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Enter Verification Code",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "We've sent a 6-digit code to your $email",
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = otp,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    onOtpChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 8.sp
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = "------",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.LightGray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEFF1F8),
                unfocusedContainerColor = Color(0xFFEFF1F8),
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = Color.Transparent,
            )
        )

        if (state is AuthState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't receive a code?"
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onResendClick
            ) {
                Text(
                    text = "Resend Code",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp)
                .height(56.dp),
            enabled = otp.length == 6 && state !is AuthState.Loading
        ) {
            if (state is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    "Verify & Continue", style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "lock icon",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.align(Alignment.Bottom),
                text = "Securely encrypted verification ",
                style = TextStyle(
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreenTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.verification),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(id = R.string.back_button_content_description),
                    tint = EmeraldDark
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@SuppressLint("RememberInComposition")
@Preview(showBackground = true)
@Composable
fun OtpScreenPreview() {
    WellMeTheme {
        OtpScreenContent(
            email = "user@example.com",
            otp = "",
            onOtpChange = {},
            state = AuthState.Idle,
            onVerifyClick = {},
            onResendClick = {},
            focusRequester = FocusRequester()
        )
    }
}
