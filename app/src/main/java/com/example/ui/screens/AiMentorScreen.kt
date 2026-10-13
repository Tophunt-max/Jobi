package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AiResponseState
import com.example.model.JobEntity
import com.example.model.UserEligibilityProfile
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AiMentorViewModel

@Composable
fun AiMentorScreen(
    userProfile: UserEligibilityProfile,
    allJobs: List<JobEntity>,
    viewModel: AiMentorViewModel,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val responseState by viewModel.responseState.collectAsState()
    val queryTitle by viewModel.currentQueryTitle.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // 1. Header Card with Thinking Mode Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DeepNavy, Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SaffronOrange,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GEMINI 3.1 PRO • THINKING LEVEL HIGH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PureWhite.copy(alpha = 0.15f))
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AI Sarkari Career & Legal Eligibility Mentor",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Uses high-order reasoning to analyze Indian reservation policies, OBC-NCL crucial dates, age relaxation calculations, and high-scoring exam preparation strategies.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PureWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Candidate Active Profile Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Evaluation Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.age} Yrs • ${userProfile.category.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${userProfile.qualification.displayName} • ${userProfile.homeState}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToProfile,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeepNavy),
                    modifier = Modifier.testTag("edit_profile_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = DeepNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 11.sp,
                        color = DeepNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Quick Action Chips
        Text(
            text = "Suggested Inquiries",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Slate800
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Option A: Evaluate against top active job
            val topJob = allJobs.firstOrNull()
            if (topJob != null) {
                Surface(
                    onClick = { viewModel.evaluateJobEligibility(userProfile, topJob) },
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_top_job_eligibility")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚖️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Check Eligibility for ${topJob.title.take(35)}...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Computes age cutoff math & reservation rules",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            }

            // Option B: 90 Day Blueprint for SSC CGL
            Surface(
                onClick = { viewModel.getExamPreparationStrategy("SSC CGL 2026", null) },
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_ssc_strategy")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "90-Day Strategy for SSC CGL 2026",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "High-weightage topic allocation & mock test routine",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }
                }
            }

            // Option C: Clarify OBC-NCL & EWS certificate rules
            Surface(
                onClick = {
                    viewModel.askCustomQuestion(
                        "What is the crucial date for OBC-NCL and EWS certificate validity in Central Government Recruitments? Explain financial year requirements.",
                        userProfile
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_certificate_rules")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📜", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OBC-NCL & EWS Crucial Date Rules",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "Financial year criteria, Annexure formats, DoPT rules",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Custom Inquiry Input Field
        Text(
            text = "Ask Any Government Recruitment Question",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Slate800
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = {
                    Text(
                        text = "e.g. Can final year engineering students apply for ISRO?",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedBorderColor = DeepNavy,
                    unfocusedBorderColor = Slate200,
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_custom_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.askCustomQuestion(inputQuery, userProfile)
                    inputQuery = ""
                },
                enabled = inputQuery.isNotBlank() && responseState !is AiResponseState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("ai_send_query_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Query",
                    tint = PureWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Response Display Section
        when (val state = responseState) {
            is AiResponseState.Loading -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SaffronOrange,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Analyzing with High Thinking Mode...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cross-referencing DoPT reservation policies, educational codes & advertisement rules.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            is AiResponseState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_response_success_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(NavyContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (queryTitle.isNotBlank()) queryTitle else "Mentor Advice",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    maxLines = 1
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, state.responseText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share AI Advice"))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Slate600
                                    )
                                }
                                IconButton(onClick = { viewModel.resetState() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset",
                                        tint = Slate600
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

                        Text(
                            text = state.responseText,
                            fontSize = 13.sp,
                            color = Slate900,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Disclaimer: AI guidance is for information only. Always refer to original official advertisements before applying.",
                                fontSize = 10.sp,
                                color = Slate500,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            is AiResponseState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Advisory Response",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.fallbackAnswer ?: state.errorMessage,
                            fontSize = 13.sp,
                            color = Slate800,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            is AiResponseState.Idle -> {
                // Helpful instructional guidance when idle
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 How High Thinking AI Evaluates You",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Exact Cutoff Date Calculation: Applies category relaxations (OBC +3 yrs, SC/ST +5 yrs, PwD +10 yrs) precisely against the official gazette reference date.\n\n2. Reservation Status Audit: Checks whether you qualify for State vs Central reservation benefits based on your state domicile.\n\n3. Syllabus & PYQ Blueprint: Formulates realistic study roadmaps based on official marking schemes.",
                            fontSize = 12.sp,
                            color = Slate600,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
