package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobEntity
import com.example.util.AppUtils
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GoldStar
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
import com.example.ui.theme.VerifiedBlue

@Composable
fun JobCard(
    job: JobEntity,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dynamic brand badge colors based on organization category
    val (orgMonogram, gradientColors) = when {
        job.category.contains("Admit Card", ignoreCase = true) || job.title.contains("Admit Card", ignoreCase = true) -> "CARD" to listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
        job.category.contains("Result", ignoreCase = true) || job.title.contains("Result", ignoreCase = true) -> "RSLT" to listOf(Color(0xFF059669), Color(0xFF10B981))
        job.organization.contains("UPSC", ignoreCase = true) -> "UPSC" to listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
        job.organization.contains("SSC", ignoreCase = true) -> "SSC" to listOf(Color(0xFF4338CA), Color(0xFF6366F1))
        job.organization.contains("Railway", ignoreCase = true) -> "RRB" to listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
        job.organization.contains("Bank", ignoreCase = true) || job.organization.contains("SBI", ignoreCase = true) -> "BANK" to listOf(Color(0xFF0369A1), Color(0xFF0EA5E9))
        job.organization.contains("Police", ignoreCase = true) || job.organization.contains("Defense", ignoreCase = true) -> "DEF" to listOf(Color(0xFFBE123C), Color(0xFFF43F5E))
        job.organization.contains("ISRO", ignoreCase = true) -> "ISRO" to listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
        else -> job.category.take(4).uppercase() to listOf(Color(0xFF1E293B), Color(0xFF334155))
    }

    val context = LocalContext.current
    val countdownText = AppUtils.getCountdownBadgeText(job.applicationClosingDate)
    val remainingDays = AppUtils.getRemainingDays(job.applicationClosingDate)
    val isUrgent = remainingDays <= 3
    val isSoon = remainingDays in 4..7

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isUrgent) 1.5.dp else 1.dp,
            color = if (isUrgent) StatusUrgent.copy(alpha = 0.5f) else Slate200.copy(alpha = 0.8f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Monogram + Org Name + Category Badge + Urgency Countdown Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orgMonogram,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = job.organization,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // Category Tag Chip
                            Surface(
                                color = NavyContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = job.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Advt: ${job.advtNumber}",
                                fontSize = 11.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (job.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Official Source",
                                    tint = VerifiedBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dynamic Urgency & Countdown Badge
                Surface(
                    color = when {
                        isUrgent -> StatusUrgentBg
                        isSoon -> Color(0xFFFEF3C7)
                        else -> GreenContainer
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isUrgent -> StatusUrgent.copy(alpha = 0.5f)
                            isSoon -> Color(0xFFF59E0B).copy(alpha = 0.5f)
                            else -> EmeraldMint.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isUrgent -> StatusUrgent
                                        isSoon -> Color(0xFFF59E0B)
                                        else -> EmeraldMint
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = countdownText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isUrgent -> StatusUrgent
                                isSoon -> Color(0xFFB45309)
                                else -> OnGreenContainer
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Job Notice Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Slate900,
                    lineHeight = 21.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Eligibility & Salary Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Vacancies Chip
                Surface(
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ ${job.totalVacancies} Posts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }

                // Qualification Chip
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.qualification,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Salary Scale Chip
                Surface(
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (job.salaryScale.isNotBlank()) job.salaryScale.take(18) else "7th CPC Pay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    }
                }

                // Age Limit Chip
                Surface(
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${job.minAge}-${job.maxAge} Yrs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                // State / Jurisdiction Chip
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "📍 ${job.stateOrAllIndia}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Direct Action Bar (PDF Notice, Apply Online, Share, Bookmark, View Details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick PDF Notice Button
                OutlinedButton(
                    onClick = {
                        val pdfUrl = job.officialNotificationUrl.ifEmpty { job.officialWebsiteUrl }
                        AppUtils.openOfficialPdf(context, pdfUrl)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RoyalBlue,
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("quick_pdf_btn_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "PDF Notice",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Quick Apply Online Button
                OutlinedButton(
                    onClick = {
                        val applyUrl = job.applyOnlineUrl.ifEmpty { job.officialWebsiteUrl }
                        AppUtils.openApplyPortal(context, applyUrl)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldGreen,
                        containerColor = Color(0xFFF0FDF4)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("quick_apply_btn_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Apply Portal",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick Calendar
                IconButton(
                    onClick = { AppUtils.addJobDeadlineToCalendar(context, job) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("calendar_btn_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Add to Calendar",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Share Job Alert
                IconButton(
                    onClick = {
                        if (onShare != {}) onShare() else AppUtils.shareJobAlert(context, job)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("share_btn_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Job Alert",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bookmark
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("bookmark_btn_${job.id}")
                ) {
                    Icon(
                        imageVector = if (job.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (job.isBookmarked) "Remove from Favorites" else "Save to Favorites",
                        tint = if (job.isBookmarked) GoldStar else Slate400,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Details Button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("view_details_btn_${job.id}")
                ) {
                    Text(
                        text = "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
