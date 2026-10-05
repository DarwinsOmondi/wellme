package com.example.wellme.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.WellMeTheme
import kotlin.math.roundToInt

@Composable
fun SwipeToConfirm(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit
) {
    var dragAmount by remember { mutableStateOf(0f) }
    var maxDragWidth by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thumbSize = 48.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFF8F9FA))
            .onSizeChanged {
                maxDragWidth = it.width.toFloat() - thumbSizePx
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Background track text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = !isConfirmed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Swipe to Pay",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            AnimatedVisibility(
                visible = isConfirmed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Payment Sent!",
                    color = PrimaryBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Draggable Thumb
        if (!isConfirmed) {
            Surface(
                modifier = Modifier
                    .offset { IntOffset(dragAmount.roundToInt(), 0) }
                    .size(thumbSize + 8.dp)
                    .padding(4.dp)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val newValue = dragAmount + delta
                            dragAmount = newValue.coerceIn(0f, maxDragWidth)
                        },
                        onDragStopped = {
                            if (dragAmount >= maxDragWidth * 0.9f) {
                                dragAmount = maxDragWidth
                                isConfirmed = true
                                onConfirm()
                            } else {
                                // Snap back
                                dragAmount = 0f
                            }
                        }
                    ),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                // Empty white thumb as in screenshot
            }
        } else {
            Surface(
                modifier = Modifier
                    .offset { IntOffset(maxDragWidth.roundToInt(), 0) }
                    .size(thumbSize + 8.dp)
                    .padding(4.dp),
                shape = CircleShape,
                color = PrimaryBlue,
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirmed",
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeToConfirmPreview() {
    WellMeTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            SwipeToConfirm(
                onConfirm = {}
            )
        }
    }
}
