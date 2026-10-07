package com.example.wellme.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.presentation.student.SheetState
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@Composable
fun BottomSheetContent(
    state: SheetState,
    onDismiss: () -> Unit,
    onScan: (String, Long) -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (state) {
                is SheetState.Idle -> {
                    // Manual Scan Entry or Prompt
                    var merchantIdInput by remember { mutableStateOf("") }
                    var amountInput by remember { mutableStateOf("") }

                    Text(
                        text = "Enter Payment Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = merchantIdInput,
                        onValueChange = { merchantIdInput = it },
                        label = { Text("Vendor / Merchant ID") },
                        placeholder = { Text("e.g. campus_cafe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountInput = it },
                        label = { Text("Amount (KSh)") },
                        placeholder = { Text("500") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (merchantIdInput.isNotBlank() && amt > 0) {
                                onScan(merchantIdInput.trim(), (amt * 100).toLong())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Proceed to Pay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is SheetState.Scanned -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.merchant.businessName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Text(
                                text = "Verified WellMe Merchant",
                                fontSize = 13.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "${(state.merchant.discountTier * 100).toInt()}% OFF",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Price Breakdown Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF8F9FA),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Original Amount", color = Color.Gray)
                                Text(String.format(Locale.getDefault(), "KSh %,.2f", state.originalAmountInCents / 100.0), fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Community Discount", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                                Text(
                                    text = String.format(Locale.getDefault(), "-KSh %,.2f", state.discountAppliedInCents / 100.0),
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE0E0E0))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = String.format(Locale.getDefault(), "KSh %,.2f", state.netAmountInCents / 100.0),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SwipeToConfirm(
                        onConfirm = onConfirm
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                is SheetState.Processing -> {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Text(
                        text = "Processing M-Pesa STK Push...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Please check your phone and enter your M-Pesa PIN.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
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
                        text = "STK Push Sent Successfully!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Complete the prompt on your phone to update your wallet.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
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
                        text = "Request Failed",
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
                        Text("Dismiss")
                    }
                }
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
                merchant = MerchantProfile("1", "Campus Cafe", 0.15, 1000, 500, true),
                originalAmountInCents = 50000,
                discountAppliedInCents = 7500,
                netAmountInCents = 42500
            ),
            onDismiss = {},
            onScan = { _, _ -> },
            onConfirm = {}
        )
    }
}
