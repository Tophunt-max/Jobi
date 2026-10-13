package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationItem
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusUrgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<NotificationItem>,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit,
    onSyncDeadlines: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterType by remember { mutableStateOf("ALL") } // ALL, DEADLINE, OTHER

    val filteredList = remember(notifications, filterType) {
        when (filterType) {
            "DEADLINE" -> notifications.filter { it.type == "DEADLINE_ALERT" }
            "OTHER" -> notifications.filter { it.type != "DEADLINE_ALERT" }
            else -> notifications
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recruitment & Deadline Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "${notifications.size} total alerts",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark read", fontSize = 12.sp, color = SaffronOrange)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs & Sync Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = filterType == "ALL",
                        onClick = { filterType = "ALL" },
                        label = { Text("All (${notifications.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepNavy,
                            selectedLabelColor = PureWhite
                        )
                    )
                    FilterChip(
                        selected = filterType == "DEADLINE",
                        onClick = { filterType = "DEADLINE" },
                        label = {
                            val count = notifications.count { it.type == "DEADLINE_ALERT" }
                            Text("⏰ Deadlines ($count)", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusUrgent,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                if (onSyncDeadlines != null) {
                    ElevatedButton(
                        onClick = onSyncDeadlines,
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = NavyContainer),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Alerts", fontSize = 11.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (filterType == "DEADLINE") "No deadline alerts yet. Bookmark jobs to get alerts!" else "No alerts at this moment",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredList) { notif ->
                        val isDeadlineAlert = notif.type == "DEADLINE_ALERT"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                !notif.isRead && isDeadlineAlert -> StatusUrgent.copy(alpha = 0.08f)
                                !notif.isRead -> NavyContainer.copy(alpha = 0.35f)
                                else -> Color.Transparent
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDeadlineAlert) StatusUrgent.copy(alpha = 0.15f)
                                            else NavyContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDeadlineAlert) Icons.Default.Alarm else Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (isDeadlineAlert) StatusUrgent else DeepNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (notif.priority == "URGENT" || isDeadlineAlert) StatusUrgent else DeepNavy,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDeadlineAlert) StatusUrgent else SaffronOrange)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.body,
                                        fontSize = 12.sp,
                                        color = Slate600,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
