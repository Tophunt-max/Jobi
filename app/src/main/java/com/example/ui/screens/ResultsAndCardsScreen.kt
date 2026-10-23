package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.util.AppUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ads.NativeSponsoredJobCard
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PictureAsPdf
import android.app.Activity
import android.widget.Toast
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ads.AdManager
import com.example.ads.RewardedUnlockDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.AppLanguage
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import com.example.ui.components.ExamPatternCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
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

@Composable
fun ResultsAndCardsScreen(
    language: AppLanguage,
    results: List<ResultItem>,
    admitCards: List<AdmitCardItem>,
    syllabi: List<SyllabusItem>,
    answerKeys: List<AnswerKeyItem>,
    admissions: List<AdmissionItem> = emptyList(),
    initialSubTab: Int = 0,
    onSubTabChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    val tabItems = remember(results, admitCards, syllabi, admissions, answerKeys) {
        listOf(
            Triple("Results", results.size, "🏆"),
            Triple("Admit Cards", admitCards.size, "🎫"),
            Triple("Syllabus", syllabi.size, "📚"),
            Triple("Admission", admissions.size, "🎓"),
            Triple("Answer Keys", answerKeys.size, "🔑")
        )
    }
    val context = LocalContext.current

    var rewardedTargetTitle by remember { mutableStateOf<String?>(null) }
    var rewardedTargetUrl by remember { mutableStateOf<String?>(null) }

    if (rewardedTargetTitle != null) {
        RewardedUnlockDialog(
            itemTitle = rewardedTargetTitle ?: "",
            onDismiss = { rewardedTargetTitle = null },
            onWatchAdClick = {
                val act = context as? Activity
                if (act != null) {
                    AdManager.showRewardedAd(
                        activity = act,
                        onUserEarnedReward = {
                            Toast.makeText(context, "🎉 Model Paper & Solved Key Unlocked!", Toast.LENGTH_SHORT).show()
                            rewardedTargetUrl?.let { url ->
                                AppUtils.openUrl(context, url, rewardedTargetTitle ?: "Unlocked Paper")
                            }
                        },
                        onAdClosed = {
                            rewardedTargetTitle = null
                        }
                    )
                } else {
                    rewardedTargetTitle = null
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Tab Row with Live Dynamic Count Badges
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = PureWhite,
            contentColor = RoyalBlue,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedSubTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = RoyalBlue,
                        height = 3.dp
                    )
                }
            }
        ) {
            tabItems.forEachIndexed { index, (tabTitle, count, emoji) ->
                val isSelected = selectedSubTab == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedSubTab = index
                        onSubTabChange?.invoke(index)
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            Text(
                                text = tabTitle,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) RoyalBlue else Slate700
                            )
                            if (count > 0) {
                                Surface(
                                    color = if (isSelected) RoyalBlueContainer else Slate100,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = count.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RoyalBlue else Slate600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("results_subtab_$index")
                )
            }
        }

        when (selectedSubTab) {
            // 0: Results
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(results, key = { _, it -> "result_${it.id}" }, contentType = { _, _ -> "result_item" }) { index, res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("result_card_${res.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = GreenContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = res.stage,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnGreenContainer,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Declared: ${res.publicationDate}",
                                        fontSize = 11.sp,
                                        color = Slate500,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = res.examName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Text(
                                    text = "${res.recruitingOrg} • Advt: ${res.advtNumber}",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Official Cut-off Marks Summary",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = res.cutOffSummary,
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val act = context as? Activity
                                        if (act != null) {
                                            AdManager.showInterstitialIfAllowed(act) {
                                                AppUtils.openOfficialPdf(context, res.officialResultUrl, "Result: ${res.examName}")
                                            }
                                        } else {
                                            AppUtils.openOfficialPdf(context, res.officialResultUrl, "Result: ${res.examName}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Check Merit List & Result PDF",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Native Sponsored Test Series / Career Partner Card every 3 items
                        if (index > 0 && (index + 1) % 3 == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            NativeSponsoredJobCard(creativeIndex = (index / 3) % 3)
                        }
                    }
                }
            }

            // 1: Admit Cards
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(admitCards, key = { _, it -> "admit_${it.id}" }, contentType = { _, _ -> "admit_item" }) { index, card ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admit_card_${card.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                            text = card.recruitingOrg,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Released: ${card.releaseDate}",
                                        fontSize = 11.sp,
                                        color = Slate500,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = card.examName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = StatusUrgentBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "📅 ${card.examDate}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusUrgent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = card.instructions,
                                    fontSize = 11.sp,
                                    color = Slate600,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val act = context as? Activity
                                        if (act != null) {
                                            AdManager.showInterstitialIfAllowed(act) {
                                                AppUtils.openApplyPortal(context, card.officialDownloadUrl, "Admit Card: ${card.examName}")
                                            }
                                        } else {
                                            AppUtils.openApplyPortal(context, card.officialDownloadUrl, "Admit Card: ${card.examName}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Download e-Admit Card / Hall Ticket",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Native Sponsored Exam Mock & Hall Ticket Prep Kit Card every 3 items
                        if (index > 0 && (index + 1) % 3 == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            NativeSponsoredJobCard(creativeIndex = (index / 3 + 1) % 3)
                        }
                    }
                }
            }

            // 2: Syllabus
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ExamPatternCard(
                            examTitle = "All-India Government Exams Scheme of Pattern",
                            timeDuration = "Tier-1 / Preliminary Exams (60-120 Mins)",
                            negativeMarking = "0.25 to 0.50 Negative Marking"
                        )
                    }

                    itemsIndexed(syllabi, key = { _, it -> "syl_${it.id}" }, contentType = { _, _ -> "syl_item" }) { index, syl ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("syllabus_card_${syl.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = syl.examName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Text(
                                    text = syl.recruitingOrg,
                                    fontSize = 12.sp,
                                    color = Slate600
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Official Exam Pattern",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                        Text(
                                            text = syl.examPattern,
                                            fontSize = 12.sp,
                                            color = Slate700
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Negative Marking: ${syl.negativeMarkingRule}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = StatusUrgent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val act = context as? Activity
                                        if (act != null) {
                                            AdManager.showInterstitialIfAllowed(act) {
                                                AppUtils.openOfficialPdf(context, syl.officialPdfUrl, "Syllabus: ${syl.examName}")
                                            }
                                        } else {
                                            AppUtils.openOfficialPdf(context, syl.officialPdfUrl, "Syllabus: ${syl.examName}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Download Full Official Syllabus PDF",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        rewardedTargetTitle = "${syl.examName} Solved Model Test Paper & Prep Kit"
                                        rewardedTargetUrl = syl.officialPdfUrl
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎁 Unlock Solved Model Paper (Free)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                }
                            }
                        }

                        // Native Sponsored E-Book / Study Notes Partner Card every 3 items
                        if (index > 0 && (index + 1) % 3 == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            NativeSponsoredJobCard(creativeIndex = (index / 3 + 2) % 3)
                        }
                    }
                }
            }

            // 3: Admission Notices
            3 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = RoyalBlueContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🎓", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Central & State University Admissions 2026",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Official notifications for CUET, JEE, NEET, B.Ed, ITI & Polytechnic entrance forms",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(admissions, key = { _, it -> "adm_${it.id}" }, contentType = { _, _ -> "adm_item" }) { index, adm ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admission_card_${adm.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                            text = adm.conductingAuthority,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    val statusBg = when (adm.status) {
                                        "CLOSING_SOON" -> StatusUrgentBg
                                        "OPEN" -> GreenContainer
                                        else -> Slate100
                                    }
                                    val statusColor = when (adm.status) {
                                        "CLOSING_SOON" -> StatusUrgent
                                        "OPEN" -> OnGreenContainer
                                        else -> Slate700
                                    }
                                    Surface(
                                        color = statusBg,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = adm.status.replace("_", " "),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = adm.examOrCourseName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Eligibility & Academic Criteria",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = adm.eligibilityCourse,
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Application Window",
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                        Text(
                                            text = "${adm.applicationStartDate} to ${adm.applicationClosingDate}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate800
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Entrance Exam",
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                        Text(
                                            text = adm.entranceExamDate,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RoyalBlue
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val act = context as? Activity
                                            if (act != null) {
                                                AdManager.showInterstitialIfAllowed(act) {
                                                    AppUtils.openApplyPortal(context, adm.officialApplyUrl, "Apply Online: ${adm.examOrCourseName}")
                                                }
                                            } else {
                                                AppUtils.openApplyPortal(context, adm.officialApplyUrl, "Apply Online: ${adm.examOrCourseName}")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Apply Online",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val act = context as? Activity
                                            if (act != null) {
                                                AdManager.showInterstitialIfAllowed(act) {
                                                    AppUtils.openOfficialPdf(context, adm.officialInformationBulletinUrl, "Bulletin: ${adm.examOrCourseName}")
                                                }
                                            } else {
                                                AppUtils.openOfficialPdf(context, adm.officialInformationBulletinUrl, "Bulletin: ${adm.examOrCourseName}")
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = DeepNavy
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Bulletin PDF",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepNavy
                                        )
                                    }
                                }
                            }
                        }

                        // Native Sponsored University & Entrance Test Partner Card every 3 items
                        if (index > 0 && (index + 1) % 3 == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            NativeSponsoredJobCard(creativeIndex = (index / 3) % 3)
                        }
                    }
                }
            }

            // 4: Answer Keys
            4 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(answerKeys, key = { _, it -> "ans_${it.id}" }, contentType = { _, _ -> "ans_item" }) { index, keyItem ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("answer_key_${keyItem.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (keyItem.status == "PROVISIONAL") StatusUrgentBg else GreenContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${keyItem.status} KEY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (keyItem.status == "PROVISIONAL") StatusUrgent else OnGreenContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "Released: ${keyItem.releaseDate}",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = keyItem.examName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Text(
                                    text = keyItem.recruitingOrg,
                                    fontSize = 12.sp,
                                    color = Slate600
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Objection Window Deadline: ${keyItem.objectionDeadline}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate800
                                        )
                                        Text(
                                            text = "Challenge Fee: ${keyItem.objectionFee}",
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val act = context as? Activity
                                        if (act != null) {
                                            AdManager.showInterstitialIfAllowed(act) {
                                                AppUtils.openUrl(context, keyItem.officialLink, "Answer Key: ${keyItem.examName}")
                                            }
                                        } else {
                                            AppUtils.openUrl(context, keyItem.officialLink, "Answer Key: ${keyItem.examName}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "View Answer Key & Submit Objection",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Native Sponsored Answer Key & Cutoff Analysis Partner Card every 3 items
                        if (index > 0 && (index + 1) % 3 == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            NativeSponsoredJobCard(creativeIndex = (index / 3 + 1) % 3)
                        }
                    }
                }
            }
        }
    }
}
