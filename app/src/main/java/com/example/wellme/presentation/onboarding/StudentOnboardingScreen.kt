package com.example.wellme.presentation.onboarding

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.wellme.R
import com.example.wellme.theme.EmeraldDark
import com.example.wellme.theme.WellMeTheme

@Composable
fun StudentOnboardingScreen(
    viewModel: OnboardingViewModel,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val context = LocalContext.current
    StudentOnboardingContent(
        fullName = viewModel.fullName,
        onFullNameChange = viewModel::onFullNameChange,
        studentId = viewModel.studentIdNumber,
        onStudentIdChange = viewModel::onStudentIdNumberChange,
        campusEmail = viewModel.campusEmail,
        onCampusEmailChange = viewModel::onCampusEmailChange,
        phoneNumber = viewModel.phoneNumber,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onBackClick = onBackClick,
        onContinueClick = {
            if (viewModel.fullName.isBlank() || viewModel.studentIdNumber.isBlank() || viewModel.campusEmail.isBlank() || viewModel.phoneNumber.isBlank()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                onContinueClick()
            }
        }
    )
}

@Composable
fun StudentOnboardingContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    studentId: String,
    onStudentIdChange: (String) -> Unit,
    campusEmail: String,
    onCampusEmailChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            OnboardingTopBar(
                title = stringResource(R.string.fin_community),
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding()
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProgressSection(
                stepText = stringResource(R.string.step_1_of_2),
                progress = 0.5f
            )

            Spacer(modifier = Modifier.height(24.dp))

            OnboardingTextField(
                label = stringResource(R.string.full_name_label),
                value = fullName,
                onValueChange = onFullNameChange,
                placeholder = stringResource(R.string.full_name_placeholder)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OnboardingTextField(
                label = stringResource(R.string.student_id_label),
                value = studentId,
                onValueChange = onStudentIdChange,
                placeholder = stringResource(R.string.student_id_placeholder)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OnboardingTextField(
                label = stringResource(R.string.campus_email_label),
                value = campusEmail,
                onValueChange = onCampusEmailChange,
                placeholder = stringResource(R.string.campus_email_placeholder)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OnboardingTextField(
                label = "M-Pesa Phone Number",
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = "2547XXXXXXXX",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(32.dp))

            VerificationInfoBox(
                text = stringResource(R.string.verification_info_text)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_to_verification),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OnboardingTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = stringResource(R.string.back_button_content_description),
                tint = EmeraldDark,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark,
            modifier = Modifier.padding(start = 0.dp)
        )
    }
}

@Composable
fun ProgressSection(
    stepText: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_uppercase),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.personal_details),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = stepText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1C1E),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = EmeraldDark,
            trackColor = Color(0xFFE1E2EC),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun OnboardingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF74777F),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldDark,
                unfocusedBorderColor = Color(0xFFC4C6D0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun VerificationInfoBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF0F4FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_security),
                contentDescription = null,
                tint = EmeraldDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF44474E),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentOnboardingScreenPreview() {
    WellMeTheme {
        StudentOnboardingContent(
            fullName = "",
            onFullNameChange = {},
            studentId = "",
            onStudentIdChange = {},
            campusEmail = "",
            onCampusEmailChange = {},
            phoneNumber = "254",
            onPhoneNumberChange = {}
        )
    }
}
