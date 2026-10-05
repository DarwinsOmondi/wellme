package com.example.wellme.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import com.example.wellme.util.GuestSession

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToSignIn: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.updateProfileImage(context.contentResolver, it) }
        }
    )

    LaunchedEffect(state) {
        if (state is ProfileUiState.SignedOut) {
            onSignOut()
        } else if (state is ProfileUiState.Error) {
            snackbarHostState.showSnackbar((state as ProfileUiState.Error).message)
        }
    }

    ProfileScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAvatarClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onBackClick = onBack,
        onSignOutClick = viewModel::signOut,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToSignIn = onNavigateToSignIn
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onAvatarClick: () -> Unit,
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    val isGuest = GuestSession.isGuest

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        "WellMe",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                },
                actions = {
                    if (!isGuest) {
                        val avatarUrl = (state as? ProfileUiState.Success)?.profile?.avatarUrl
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable(onClick = onAvatarClick)
                        ) {
                            AsyncImage(
                                model = avatarUrl ?: "https://ui-avatars.com/api/?name=User&background=random",
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8F9FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (isGuest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Guest User",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You are currently browsing the app in guest mode. Sign in or create an account to access your secure wallet, transaction history, and full features.",
                        fontSize = 15.sp,
                        color = Color(0xFF44474E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            GuestSession.isGuest = false
                            onNavigateToSignIn()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Sign In / Register",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                when (state) {
                    is ProfileUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }
                    is ProfileUiState.Success -> {
                        val profile = state.profile
                        
                        Text(
                            text = profile.fullName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                        
                        Text(
                            text = profile.userId,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = "Account Settings",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column {
                                SettingsItem(
                                    icon = Icons.Default.PersonOutline,
                                    title = "Personal Details",
                                    onClick = { onNavigateToDetail("personal") }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
                                SettingsItem(
                                    icon = Icons.Default.Security,
                                    title = "Security",
                                    onClick = { onNavigateToDetail("security") }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
                                SettingsItem(
                                    icon = Icons.Default.NotificationsNone,
                                    title = "Notification Preferences",
                                    onClick = { onNavigateToDetail("notifications") }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
                                SettingsItem(
                                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                                    title = "Help & Support",
                                    onClick = { onNavigateToDetail("help") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = onSignOutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFEBEE),
                                contentColor = Color(0xFFB71C1C)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Sign Out",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    is ProfileUiState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {
                         // Idle
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlueLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF44474E)
        )
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    WellMeTheme {
        ProfileScreenContent(
            state = ProfileUiState.Success(
                UserProfile(
                    fullName = "Alex Kamau",
                    userId = "STUDENT ID: WM-2024-8842",
                    avatarUrl = null
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAvatarClick = {},
            onBackClick = {},
            onSignOutClick = {},
            onNavigateToDetail = {},
            onNavigateToSignIn = {}
        )
    }
}
