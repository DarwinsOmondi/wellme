package com.example.wellme.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wellme.R
import com.example.wellme.theme.WellMeTheme


@Composable
fun ScannerOverlay(
    onClose: () -> Unit,
    onScan: (String, Long) -> Unit,
    showSimulateButton: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Top Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = { /* Flash */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flash),
                        contentDescription = "Flash",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { /* Help */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_help),
                        contentDescription = "Help",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val bracketSize = 48.dp
                val stroke = 3.dp
                val color = Color.White
                val radius = 16.dp

                // Brackets matching the screenshot style (L-shapes)
                Box(Modifier
                    .align(Alignment.TopStart)
                    .size(bracketSize)) {
                    Box(
                        Modifier
                            .height(stroke)
                            .width(bracketSize)
                            .background(color, RoundedCornerShape(radius))
                    )
                    Box(
                        Modifier
                            .width(stroke)
                            .height(bracketSize)
                            .background(color, RoundedCornerShape(radius))
                    )
                }
                Box(Modifier
                    .align(Alignment.TopEnd)
                    .size(bracketSize)) {
                    Box(
                        Modifier
                            .height(stroke)
                            .width(bracketSize)
                            .align(Alignment.TopEnd)
                            .background(color, RoundedCornerShape(radius))
                    )
                    Box(
                        Modifier
                            .width(stroke)
                            .height(bracketSize)
                            .align(Alignment.TopEnd)
                            .background(color, RoundedCornerShape(radius))
                    )
                }
                Box(Modifier
                    .align(Alignment.BottomStart)
                    .size(bracketSize)) {
                    Box(
                        Modifier
                            .height(stroke)
                            .width(bracketSize)
                            .align(Alignment.BottomStart)
                            .background(color, RoundedCornerShape(radius))
                    )
                    Box(
                        Modifier
                            .width(stroke)
                            .height(bracketSize)
                            .align(Alignment.BottomStart)
                            .background(color, RoundedCornerShape(radius))
                    )
                }
                Box(Modifier
                    .align(Alignment.BottomEnd)
                    .size(bracketSize)) {
                    Box(
                        Modifier
                            .height(stroke)
                            .width(bracketSize)
                            .align(Alignment.BottomEnd)
                            .background(color, RoundedCornerShape(radius))
                    )
                    Box(
                        Modifier
                            .width(stroke)
                            .height(bracketSize)
                            .align(Alignment.BottomEnd)
                            .background(color, RoundedCornerShape(radius))
                    )
                }
            }

            if (showSimulateButton) {
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { onScan("campus_cafe", 15000L) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🔋 Simulate Scan (Campus Cafe • KSh 150)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScannerOverlayPreview() {
    WellMeTheme {
        ScannerOverlay(
            onClose = {},
            onScan = { _, _ -> }
        )
    }
}