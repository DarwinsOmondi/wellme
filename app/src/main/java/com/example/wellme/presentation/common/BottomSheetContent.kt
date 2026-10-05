package com.example.wellme.presentation.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.presentation.student.SheetState
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun BottomSheetContent(
    state: SheetState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (state) {
                is SheetState.Scanned -> {
                    // Merchant Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFFEEF2FF)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_store),
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = state.merchant.businessName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_verified),
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Merchant",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Price Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Original Price",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "KSh %,.2f", state.originalAmountInCents / 100.0),
                            color = Color.Black,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Discount Badge
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlueLight
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tag),
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "WellMe Discount (%.0f%%)", state.merchant.discountTier * 100),
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "-KSh %,.2f", state.discountAppliedInCents / 100.0),
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Deducted ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "KSh %,.2f", state.netAmountInCents / 100.0),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    SwipeToConfirm(
                        onConfirm = onConfirm
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                is SheetState.Processing -> {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Text(
                        text = "Processing Payment...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                is SheetState.Success -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_verified),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Payment Successful!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Done")
                    }
                }
                is SheetState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Payment Failed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Red
                    )
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Gray
                    )
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomSheetContentPreview() {
    WellMeTheme {
        BottomSheetContent(
            state = SheetState.Scanned(
                merchant = MerchantProfile(
                    merchantId = "campus_cafe",
                    businessName = "Campus Cafe",
                    discountTier = 0.15,
                    poolTargetInCents = 1000000L,
                    poolRaisedInCents = 500000L,
                    isVerified = true
                ),
                originalAmountInCents = 15000L,
                discountAppliedInCents = 2250L,
                netAmountInCents = 12750L
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
