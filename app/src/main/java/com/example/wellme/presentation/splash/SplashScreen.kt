package com.example.wellme.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.presentation.auth.AuthViewModel
import com.example.wellme.theme.PrimaryBlue

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onNavigateToStudentMain: () -> Unit,
    onNavigateToMerchantMain: () -> Unit,
    onNavigateToOnboarding: (String, String) -> Unit,
) {
    LaunchedEffect(Unit) {
        // Check session
        val user = viewModel.getCurrentUser()
        if (user == null) {
            onNavigateToSignIn()
        } else {
            // Check KYC status to decide destination
            viewModel.checkKycAndRoute(
                userId = user.id,
                email = user.email ?: "",
                onStudentMain = onNavigateToStudentMain,
                onMerchantMain = onNavigateToMerchantMain,
                onOnboarding = onNavigateToOnboarding
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "WellMe Logo",
                modifier = Modifier.size(120.dp),
                tint = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "WellMe",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = PrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
