package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ads.NativeSponsoredJobCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.JobEntity
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorSheet(
    allJobs: List<JobEntity>,
    onSelectJob: (JobEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Current State
    var birthYear by remember { mutableIntStateOf(2000) }
    var birthMonth by remember { mutableIntStateOf(7) } // 1-12
    var birthDay by remember { mutableIntStateOf(15) }

    var selectedCategory by remember { mutableStateOf("OBC-NCL") }
    val categories = listOf(
        "General (UR)" to 0,
        "OBC-NCL" to 3,
        "SC" to 5,
        "ST" to 5,
        "EWS" to 0,
        "PwD (UR)" to 10,
        "PwD (OBC)" to 13,
        "PwD (SC/ST)" to 15
    )

    // Calculate Exact Age as of 01-08-2026 (standard cut-off for 2026 civil services/SSC)
    val cutoffYear = 2026
    val cutoffMonth = 8
    val cutoffDay = 1

    val ageCalculation by remember(birthYear, birthMonth, birthDay) {
        derivedStateOf {
            var years = cutoffYear - birthYear
            var months = cutoffMonth - birthMonth
            var days = cutoffDay - birthDay

            if (days < 0) {
                months -= 1
                days += 30
            }
            if (months < 0) {
                years -= 1
                months += 12
            }
            Triple(years.coerceAtLeast(0), months.coerceAtLeast(0), days.coerceAtLeast(0))
        }
    }

    val relaxationYears = categories.firstOrNull { it.first == selectedCategory }?.second ?: 0

    // Filter matching jobs based on calculated age with relaxation
    val matchingJobs by remember(ageCalculation, relaxationYears, allJobs) {
        derivedStateOf {
            val userAge = ageCalculation.first
            allJobs.filter { job ->
                val effectiveMaxAge = job.maxAge + relaxationYears
                // Jobs without published age limits can't be evaluated here.
                job.maxAge > 0 && userAge in job.minAge..effectiveMaxAge
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("age_calculator_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = RoyalBlueContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Cake,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Age & Eligibility Calculator",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepNavy
                            )
                            Text(
                                text = "As per Official Gazette cut-off rules (01-08-2026)",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate500
                        )
                    }
                }
            }

            // Calculation Result Banner
            item {
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR EXACT CALCULATED AGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Surface(
                                color = GreenContainer,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (relaxationYears > 0) "+$relaxationYears Yrs Relaxation Applied" else "Standard (UR)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnGreenContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${ageCalculation.first} Years, ${ageCalculation.second} Months, ${ageCalculation.third} Days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF14532D)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reference Cut-off Date: 01 August 2026 • Category: $selectedCategory",
                            fontSize = 11.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            // Date of Birth Selectors
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "1. Select Your Date of Birth",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Year Pill Selector
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(text = "Year: $birthYear", fontSize = 11.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { if (birthYear > 1985) birthYear-- },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                                    Text(text = "$birthYear", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

                                    Button(
                                        onClick = { if (birthYear < 2008) birthYear++ },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                            }

                            // Month Pill Selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Month: $birthMonth", fontSize = 11.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { if (birthMonth > 1) birthMonth-- },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                                    Text(text = "$birthMonth", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

                                    Button(
                                        onClick = { if (birthMonth < 12) birthMonth++ },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                            }

                            // Day Pill Selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Day: $birthDay", fontSize = 11.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { if (birthDay > 1) birthDay-- },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                                    Text(text = "$birthDay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)

                                    Button(
                                        onClick = { if (birthDay < 31) birthDay++ },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate200, contentColor = Slate800)
                                    ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }

            // Category Selection Chips
            item {
                Column {
                    Text(
                        text = "2. Select Reservation Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(4).forEach { (catName, _) ->
                            val isSelected = selectedCategory == catName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = catName },
                                label = { Text(catName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(4).forEach { (catName, _) ->
                            val isSelected = selectedCategory == catName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = catName },
                                label = { Text(catName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }

            // Eligible Matching Jobs Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matching Government Vacancies (${matchingJobs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepNavy
                    )
                    Text(
                        text = "Age: ${ageCalculation.first} Yrs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }

            items(matchingJobs) { job ->
                val effectiveMax = job.maxAge + relaxationYears
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onSelectJob(job)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GreenContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 1
                            )
                            Text(
                                text = "${job.organization} • Allowed: ${job.minAge} to $effectiveMax Yrs",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                NativeSponsoredJobCard(creativeIndex = 0)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
