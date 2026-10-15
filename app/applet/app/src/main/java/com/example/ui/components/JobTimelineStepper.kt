package com.example.ui.components

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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobEntity
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusUrgentBg
import com.example.util.AppUtils

enum class TimelineStepState {
    COMPLETED,
    ACTIVE,
    URGENT,
    UPCOMING,
    PENDING
}

data class TimelineStep(
    val stepNumber: Int,
    val title: String,
    val dateText: String,
    val subtitle: String,
    val icon: ImageVector,
    val state: TimelineStepState
)

@Composable
fun JobTimelineStepper(
    job: JobEntity,
    modifier: Modifier = Modifier
) {
    val remainingDays = AppUtils.getRemainingDays(job.applicationClosingDate)
    val isAppUrgent = remainingDays in 0..3
    val isAppClosed = remainingDays < 0

    val steps = listOf(
        TimelineStep(
            stepNumber = 1,
            title = "Notification Released",
            dateText = job.applicationStartDate,
            subtitle = "Official Gazette Advt No: ${job.advtNumber}",
            icon = Icons.Default.Description,
            state = TimelineStepState.COMPLETED
        ),
        TimelineStep(
            stepNumber = 2,
            title = "Online Application Window",
            dateText = "${job.applicationStartDate} to ${job.applicationClosingDate}",
            subtitle = when {
                isAppClosed -> "Application window closed"
                isAppUrgent -> "🚨 Closes in $remainingDays day${if (remainingDays == 1) "" else "s"}!"
                else -> "Active registration window • $remainingDays days left"
            },
            icon = Icons.Default.AssignmentTurnedIn,
            state = when {
                isAppClosed -> TimelineStepState.COMPLETED
                isAppUrgent -> TimelineStepState.URGENT
                else -> TimelineStepState.ACTIVE
            }
        ),
        TimelineStep(
            stepNumber = 3,
            title = "e-Admit Card Release",
            dateText = job.admitCardDate.ifEmpty { "10-14 days prior to exam" },
            subtitle = "Download link available on official portal",
            icon = Icons.Default.HourglassTop,
            state = if (isAppClosed) TimelineStepState.ACTIVE else TimelineStepState.UPCOMING
        ),
        TimelineStep(
            stepNumber = 4,
            title = "Written Examination / CBT",
            dateText = job.examDate.ifEmpty { "Tentative: Nov/Dec 2026" },
            subtitle = "Tier-I / Prelims stage test schedule",
            icon = Icons.Default.Event,
            state = TimelineStepState.UPCOMING
        ),
        TimelineStep(
            stepNumber = 5,
            title = "Result & Merit List",
            dateText = "Post Exam Evaluation",
            subtitle = "Cut-off marks & scorecard publication",
            icon = Icons.Default.EmojiEvents,
            state = TimelineStepState.PENDING
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_timeline_stepper")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoyalBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recruitment Lifecycle Timeline",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Step-by-step progress tracking for applicants",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            steps.forEachIndexed { index, step ->
                TimelineStepRow(
                    step = step,
                    isLast = index == steps.size - 1
                )
            }
        }
    }
}

@Composable
private fun TimelineStepRow(
    step: TimelineStep,
    isLast: Boolean
) {
    val (iconColor, containerColor, borderColor, statusLabel, statusBg, statusTextColor) = when (step.state) {
        TimelineStepState.COMPLETED -> StateVisuals(
            iconColor = PureWhite,
            containerColor = EmeraldGreen,
            borderColor = EmeraldMint,
            statusLabel = "Completed",
            statusBg = GreenContainer,
            statusTextColor = OnGreenContainer
        )
        TimelineStepState.ACTIVE -> StateVisuals(
            iconColor = PureWhite,
            containerColor = RoyalBlue,
            borderColor = Color(0xFF60A5FA),
            statusLabel = "In Progress",
            statusBg = RoyalBlueContainer,
            statusTextColor = DeepNavy
        )
        TimelineStepState.URGENT -> StateVisuals(
            iconColor = PureWhite,
            containerColor = StatusUrgent,
            borderColor = Color(0xFFFCA5A5),
            statusLabel = "Closing Soon",
            statusBg = StatusUrgentBg,
            statusTextColor = StatusUrgent
        )
        TimelineStepState.UPCOMING -> StateVisuals(
            iconColor = Slate600,
            containerColor = Slate100,
            borderColor = Slate300,
            statusLabel = "Scheduled",
            statusBg = Slate100,
            statusTextColor = Slate700
        )
        TimelineStepState.PENDING -> StateVisuals(
            iconColor = Slate400,
            containerColor = Slate50,
            borderColor = Slate200,
            statusLabel = "Next Stage",
            statusBg = Slate50,
            statusTextColor = Slate500
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical step node with connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                border = BorderStroke(2.dp, borderColor),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (step.state == TimelineStepState.COMPLETED) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(
                            if (step.state == TimelineStepState.COMPLETED) EmeraldGreen.copy(alpha = 0.5f)
                            else Slate200
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Step Information
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (step.state == TimelineStepState.PENDING) Slate600 else Slate900
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = step.dateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (step.state) {
                    TimelineStepState.URGENT -> StatusUrgent
                    TimelineStepState.ACTIVE -> RoyalBlue
                    TimelineStepState.COMPLETED -> EmeraldGreen
                    else -> Slate700
                },
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = step.subtitle,
                fontSize = 11.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

private data class StateVisuals(
    val iconColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val statusLabel: String,
    val statusBg: Color,
    val statusTextColor: Color
)
