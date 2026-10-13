package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class ExamSubject(
    val name: String,
    val questions: Int,
    val marks: Int,
    val topics: List<String>
)

@Composable
fun ExamPatternCard(
    examTitle: String = "Combined Recruitment Exam (Tier-1)",
    timeDuration: String = "60 Minutes",
    negativeMarking: String = "0.50 Marks per wrong answer",
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val defaultSubjects = remember {
        listOf(
            ExamSubject(
                name = "General Intelligence & Reasoning",
                questions = 25,
                marks = 50,
                topics = listOf("Analogies", "Coding-Decoding", "Syllogisms", "Venn Diagrams", "Blood Relations")
            ),
            ExamSubject(
                name = "General Awareness & Current Affairs",
                questions = 25,
                marks = 50,
                topics = listOf("Indian History & Polity", "Geography", "Current Affairs 2026", "Static GK & Science")
            ),
            ExamSubject(
                name = "Quantitative Aptitude (Mathematics)",
                questions = 25,
                marks = 50,
                topics = listOf("Percentage & Profit-Loss", "Time & Work", "Ratio & Proportion", "Algebra & Geometry", "Data Interpretation")
            ),
            ExamSubject(
                name = "English Comprehension",
                questions = 25,
                marks = 50,
                topics = listOf("Reading Comprehension", "Error Spotting", "Idioms & Phrases", "One Word Substitution")
            )
        )
    }

    // Keep track of completed topics for prep tracker
    val completedTopics = remember { mutableStateListOf<String>("Analogies", "Indian History & Polity") }
    val allTopicsCount = defaultSubjects.sumOf { it.topics.size }
    val progress = if (allTopicsCount > 0) completedTopics.size.toFloat() / allTopicsCount else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = NavyContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Exam Pattern & Syllabus Tracker",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepNavy
                        )
                        Text(
                            text = "$timeDuration • Negative: $negativeMarking",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate500
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prep Progress Bar
            Surface(
                color = Slate100,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Syllabus Readiness",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Text(
                            text = "${completedTopics.size} / $allTopicsCount Topics (${(progress * 100).toInt()}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalBlue,
                        trackColor = Slate200
                    )
                }
            }

            // Subject Breakdown Table
            Spacer(modifier = Modifier.height(12.dp))

            defaultSubjects.forEach { subject ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${subject.questions} Qs | ${subject.marks} Marks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            subject.topics.forEach { topic ->
                                val isDone = completedTopics.contains(topic)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isDone) completedTopics.remove(topic) else completedTopics.add(topic)
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isDone) EmeraldGreen else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = topic,
                                        fontSize = 11.sp,
                                        color = if (isDone) EmeraldGreen else Slate700,
                                        fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal
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
