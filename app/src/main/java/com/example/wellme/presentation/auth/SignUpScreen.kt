package com.example.wellme.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onOtpSent: (String) -> Unit,
    onNavigateToSignIn: () -> Unit
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SignUpTopBar(onBackClick = onNavigateToSignIn)
        }
    ) { padding ->
        SignUpScreenContent(
            role = viewModel.role,
            onRoleChange = viewModel::onRoleChange,
            email = viewModel.email,
            onEmailChange = viewModel::onEmailChange,
            isLoading = state is AuthState.Loading,
            onSignUpClick = viewModel::signUp,
            onNavigateToSignIn = onNavigateToSignIn,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun SignUpScreenContent(
    role: String,
    onRoleChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSignUpClick: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isButtonEnabled = email.isNotBlank() && !isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.select_your_path),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleSelectionCard(
                    roleName = stringResource(id = R.string.i_am_a_student),
                    isSelected = role == "STUDENT",
                    onClick = { onRoleChange("STUDENT") },
                    iconRes = R.drawable.ic_student,
                    iconBackgroundColor = if (role == "STUDENT") PrimaryBlueLight else Color(0xFFF0F0F0),
                    modifier = Modifier.weight(1f)
                )
                RoleSelectionCard(
                    roleName = stringResource(id = R.string.i_am_a_merchant),
                    isSelected = role == "MERCHANT",
                    onClick = { onRoleChange("MERCHANT") },
                    iconRes = R.drawable.ic_merchant,
                    iconBackgroundColor = if (role == "MERCHANT") Color(0xFFE8E8E8) else Color(0xFFF0F0F0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    SignUpInputField(
                        label = stringResource(id = R.string.email),
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = stringResource(id = R.string.email_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onSignUpClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(
                                    text = "Send Verification OTP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_forward),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.already_have_an_account),
                    color = Color.Gray
                )
                Text(
                    text = stringResource(id = R.string.sign_in),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(id = R.string.back_button_content_description),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun RoleSelectionCard(
    roleName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    iconBackgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(2.dp, PrimaryBlueLight) else null,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = roleName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SignUpInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibleChange: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = Color.LightGray) },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = onPasswordVisibleChange) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = stringResource(id = R.string.visibility_toggle_content_description),
                            tint = Color.LightGray
                        )
                    }
                }
            },
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    WellMeTheme {
        SignUpScreenContent(
            role = "STUDENT",
            onRoleChange = {},
            email = "",
            onEmailChange = {},
            isLoading = false,
            onSignUpClick = {},
            onNavigateToSignIn = {}
        )
    }
}
