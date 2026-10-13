package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JobEntity
import com.example.scheduler.DeadlineNotificationScheduler
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

@Composable
fun DeadlineSchedulerCard(
    job: JobEntity,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onTestNotification: () -> Unit,
    onScheduleCustomReminder: (daysBefore: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val daysLeft = DeadlineNotificationScheduler.calculateDaysRemaining(job.applicationClosingDate)
    val urgencyText = DeadlineNotificationScheduler.formatDaysRemainingText(job.applicationClosingDate)
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            onTestNotification()
        }
    }

    var selectedReminderDay by remember { mutableIntStateOf(3) }
    var testFeedbackSent by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (daysLeft in 0..5) StatusUrgent.copy(alpha = 0.5f) else Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("deadline_scheduler_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title + Urgency Pill
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
                            .background(if (daysLeft in 0..5) StatusUrgent.copy(alpha = 0.15f) else NavyContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.AlarmOn else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (daysLeft in 0..5) StatusUrgent else DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Deadline Alert Scheduler",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Closes: ${job.applicationClosingDate}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                // Urgency Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        daysLeft < 0 -> Slate200
                        daysLeft in 0..3 -> StatusUrgent.copy(alpha = 0.15f)
                        daysLeft in 4..7 -> SaffronOrange.copy(alpha = 0.15f)
                        else -> NavyContainer
                    }
                ) {
                    Text(
                        text = urgencyText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysLeft < 0 -> Slate600
                            daysLeft in 0..3 -> StatusUrgent
                            daysLeft in 4..7 -> SaffronOrange
                            else -> DeepNavy
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Reminder status description
            if (isBookmarked) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Automatic Alerts Scheduled",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                            Text(
                                text = "You will receive high-priority notifications 3 days and 24 hours prior to deadline.",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Never miss the last application date",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Bookmark this vacancy to automatically schedule deadline alerts.",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timing chips
            Text(
                text = "Reminder Lead Time:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate600
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to "1 Day Prior",
                    3 to "3 Days Prior",
                    7 to "7 Days Prior"
                ).forEach { (days, label) ->
                    val selected = selectedReminderDay == days
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedReminderDay = days
                            onScheduleCustomReminder(days)
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepNavy,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("reminder_chip_${days}d")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Notification Button
                OutlinedButton(
                    onClick = {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            testFeedbackSent = true
                            onTestNotification()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeepNavy),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_deadline_alert_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = DeepNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (testFeedbackSent) "Sent Alert!" else "Test Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepNavy
                    )
                }

                // Bookmark / Activate Button
                Button(
                    onClick = onBookmarkToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBookmarked) EmeraldGreen else SaffronOrange
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("toggle_reminder_bookmark_btn")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBookmarked) "Alerts Active" else "Activate Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Notifications permission is disabled. Tap 'Test Alert' to grant permission.",
                    fontSize = 11.sp,
                    color = StatusUrgent
                )
            }
        }
    }
}
