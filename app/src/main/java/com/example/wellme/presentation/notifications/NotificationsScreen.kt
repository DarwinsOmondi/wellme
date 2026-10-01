package com.example.wellme.presentation.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R

data class CampusNotification(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val type: NotificationType,
    var isRead: Boolean = false
)

enum class NotificationType {
    TOP_UP, DISCOUNT, ALLOWANCE, SECURITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    var notificationsList by remember {
        mutableStateOf(
            listOf(
                CampusNotification(
                    id = "1",
                    title = "M-Pesa Top Up Received",
                    subtitle = "KSh 1,000 deposited to your student wallet successfully.",
                    timestamp = "10m ago",
                    type = NotificationType.TOP_UP
                ),
                CampusNotification(
                    id = "2",
                    title = "Vendor Discount Applied",
                    subtitle = "15% student discount (KSh 75) processed at Campus Cafe.",
                    timestamp = "2h ago",
                    type = NotificationType.DISCOUNT
                ),
                CampusNotification(
                    id = "3",
                    title = "Allowance Verified",
                    subtitle = "Daily food allowance verified by Campus Admin.",
                    timestamp = "1d ago",
                    type = NotificationType.ALLOWANCE,
                    isRead = true
                ),
                CampusNotification(
                    id = "4",
                    title = "Security Alert",
                    subtitle = "New device login verified via OTP.",
                    timestamp = "2d ago",
                    type = NotificationType.SECURITY,
                    isRead = true
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Alerts & Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            notificationsList = notificationsList.map { it.copy(isRead = true) }
                        }
                    ) {
                        Text("Mark all as read", color = Color(0xFF006D3E), fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notificationsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notifications right now.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notificationsList, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onClick = {
                                notificationsList = notificationsList.map {
                                    if (it.id == item.id) it.copy(isRead = true) else it
                                }
                            },
                            onDismiss = {
                                notificationsList = notificationsList.filter { it.id != item.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: CampusNotification,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) Color.White else Color(0xFFE8F5E9).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (notification.isRead) Color(0xFFE0E0E0) else Color(0xFF006D3E).copy(alpha = 0.3f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = when (notification.type) {
                    NotificationType.TOP_UP -> Color(0xFFE8F5E9)
                    NotificationType.DISCOUNT -> Color(0xFFE3F2FD)
                    NotificationType.ALLOWANCE -> Color(0xFFFFF3E0)
                    NotificationType.SECURITY -> Color(0xFFFFEBEE)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (notification.type) {
                            NotificationType.TOP_UP -> Icons.Default.AccountBalanceWallet
                            NotificationType.DISCOUNT -> Icons.Default.Store
                            NotificationType.ALLOWANCE -> Icons.Default.Verified
                            NotificationType.SECURITY -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            NotificationType.TOP_UP -> Color(0xFF2E7D32)
                            NotificationType.DISCOUNT -> Color(0xFF1565C0)
                            NotificationType.ALLOWANCE -> Color(0xFFEF6C00)
                            NotificationType.SECURITY -> Color(0xFFC62828)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.subtitle,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
