package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import com.example.ads.AdManager
import com.example.ads.AdaptiveBannerAdView
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.example.ads.NativeSponsoredJobCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.AppLanguage
import com.example.model.JobEntity
import com.example.model.LanguageRegistry
import com.example.scheduler.DeadlineNotificationScheduler
import com.example.ui.components.DeadlineSchedulerCard
import com.example.ui.components.ExamPatternCard
import com.example.ui.components.SalaryCalculatorSheet
import com.example.util.AppUtils
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusUrgentBg
import com.example.ui.theme.VerifiedBlue
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    job: JobEntity,
    language: AppLanguage,
    onBack: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onCheckEligibilityWithAi: () -> Unit,
    onReportIssue: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showSalaryCalculator by remember { mutableStateOf(false) }

    // Parse Post breakdown JSON safely
    val postBreakdown = remember(job.postWiseBreakdownJson) {
        try {
            val arr = JSONArray(job.postWiseBreakdownJson)
            val list = mutableListOf<Triple<String, Int, String>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Triple(
                        obj.optString("postTitle"),
                        obj.optInt("vacancyCount"),
                        obj.optString("qualification")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Parse Category Age Relaxation JSON
    val ageRelaxations = remember(job.categoryAgeRelaxationJson) {
        try {
            val arr = JSONArray(job.categoryAgeRelaxationJson)
            val list = mutableListOf<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Pair(
                        obj.optString("categoryName"),
                        obj.optString("relaxationYears")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recruitment Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { AppUtils.addJobDeadlineToCalendar(context, job) },
                        modifier = Modifier.testTag("detail_calendar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Add to Calendar",
                            tint = PureWhite
                        )
                    }
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.testTag("detail_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (job.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (job.isBookmarked) "Remove from Favorites" else "Save to Favorites",
                            tint = if (job.isBookmarked) Color(0xFFFFA000) else PureWhite
                        )
                    }
                    IconButton(
                        onClick = { AppUtils.shareJobAlert(context, job) },
                        modifier = Modifier.testTag("detail_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = PureWhite
                )
            )
        },
        bottomBar = {
            // Adaptive Banner & Primary Application Action Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                AdaptiveBannerAdView()
                Surface(
                    color = PureWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val act = context as? Activity
                                if (act != null) {
                                    AdManager.showInterstitialIfAllowed(act) {
                                        AppUtils.openOfficialPdf(context, job.officialNotificationUrl, "Official PDF: ${job.title}")
                                    }
                                } else {
                                    AppUtils.openOfficialPdf(context, job.officialNotificationUrl, "Official PDF: ${job.title}")
                                }
                            },
                            border = BorderStroke(1.dp, DeepNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_notification_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Official PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }

                        Button(
                            onClick = {
                                val act = context as? Activity
                                if (act != null) {
                                    AdManager.showInterstitialIfAllowed(act) {
                                        AppUtils.openApplyPortal(context, job.applyOnlineUrl, "Apply Online: ${job.title}")
                                    }
                                } else {
                                    AppUtils.openApplyPortal(context, job.applyOnlineUrl, "Apply Online: ${job.title}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("apply_online_btn")
                        ) {
                            Text(
                                text = LanguageRegistry.getString("apply_online", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Organization & Source Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = NavyContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = job.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerifiedBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.verifiedSourceDomain,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VerifiedBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Job Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate900
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Organization & Advt Number
            Text(
                text = "${job.organization} • Advt No: ${job.advtNumber}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. High Thinking AI Assessment Prompt Card
            Card(
                onClick = onCheckEligibilityWithAi,
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_check_eligibility_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaffronOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Instant AI Eligibility Check",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "Deep legal analysis with Gemini 3.1 Pro High Thinking",
                                fontSize = 11.sp,
                                color = PureWhite.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Evaluate",
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Key Highlights Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recruitment Overview",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Total Vacancies", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = "${job.totalVacancies} Posts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Job Location", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = job.stateOrAllIndia,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Pay Scale", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = job.salaryScale,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                onClick = { showSalaryCalculator = true },
                                color = GreenContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = OnGreenContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "In-Hand Calc",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnGreenContainer
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Age Limit (UR)", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = if (job.maxAge > 0) "${job.minAge} to ${job.maxAge} Years" else "Refer official notification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Important Dates Section
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Important Schedule & Dates",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Application Start Date", value = job.applicationStartDate)
                    DetailRow(
                        label = "Application Closing Date",
                        value = job.applicationClosingDate,
                        isHighlight = true
                    )
                    DetailRow(label = "Tentative Exam Date", value = job.examDate)
                    DetailRow(label = "Admit Card Release", value = job.admitCardDate)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sponsored Exam Prep Partner Card (Mid-content placement)
            NativeSponsoredJobCard(creativeIndex = 0)

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Application Deadline Notification Scheduler
            DeadlineSchedulerCard(
                job = job,
                isBookmarked = job.isBookmarked,
                onBookmarkToggle = onBookmarkToggle,
                onTestNotification = {
                    DeadlineNotificationScheduler.triggerInstantTestAlert(context, job)
                },
                onScheduleCustomReminder = { daysBefore ->
                    DeadlineNotificationScheduler.scheduleDeadlineReminder(context, job, daysBefore)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Post-wise Vacancy Breakdown Table
            if (postBreakdown.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Post-wise Vacancy Breakdown",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        postBreakdown.forEachIndexed { index, (postTitle, count, req) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = postTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = req,
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Surface(
                                    color = NavyContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (index < postBreakdown.size - 1) {
                                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Category-wise Age Relaxation Table
            if (ageRelaxations.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category-wise Upper Age Relaxation",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Crucial date for age calculation: ${job.ageCutoffDate}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ageRelaxations.forEach { (categoryName, relaxationYears) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = categoryName,
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                                Text(
                                    text = relaxationYears,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 7. Application Fee Details
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Application Fees",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow(label = "General / OBC / EWS", value = job.feeGeneral)
                    DetailRow(label = "SC / ST / PwD / Female", value = job.feeReserved)
                    DetailRow(label = "Payment Mode", value = job.feeMode)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Selection Process & Detailed Eligibility
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selection Stages & Process",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = job.selectionProcess,
                        fontSize = 13.sp,
                        color = Slate800,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Detailed Eligibility",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = job.detailedEligibility,
                        fontSize = 13.sp,
                        color = Slate700,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 9. Interactive Exam Pattern & Syllabus Topic Tracker
            ExamPatternCard(
                examTitle = "${job.organization} ${job.title} Scheme of Examination",
                timeDuration = "60 Minutes (Tier-I / Prelims)",
                negativeMarking = "0.50 per wrong answer"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 9b. Native Sponsored Speed Typing & Preparation Kit
            NativeSponsoredJobCard(creativeIndex = 2)

            Spacer(modifier = Modifier.height(16.dp))

            // 10. Report Incorrect Information Action
            Surface(
                onClick = onReportIssue,
                color = Slate100,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_inaccuracy_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Report",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Report Discrepancy or Outdated Information",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Text(
                            text = "Our editorial verification desk reviews all candidate flags.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSalaryCalculator) {
        SalaryCalculatorSheet(
            initialLevel = 7,
            onDismiss = { showSalaryCalculator = false }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Slate600
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) StatusUrgent else Slate900
        )
    }
}
