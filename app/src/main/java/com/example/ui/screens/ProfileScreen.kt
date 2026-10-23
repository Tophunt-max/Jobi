package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ads.AdSettingsRepository
import com.example.ads.NativeSponsoredJobCard
import com.example.data.repository.BackendSyncState
import com.example.model.AppLanguage
import com.example.model.AppliedExamRecord
import com.example.model.AspirantDocumentLocker
import com.example.model.CasteCategory
import com.example.model.EducationLevel
import com.example.model.ExamApplicationStage
import com.example.model.JobEntity
import com.example.model.UserEligibilityProfile
import com.example.ui.components.JobCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.SaffronCoral
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.PhysicalEligibilityStatus
import com.example.util.PhysicalStandardsChecker

private val PopularProfileStates = listOf(
    "All India",
    "Uttar Pradesh",
    "Bihar",
    "Rajasthan",
    "Madhya Pradesh",
    "Delhi (NCT)",
    "Maharashtra",
    "West Bengal",
    "Haryana",
    "Punjab",
    "Jharkhand"
)

private val QuickAgeOptions = listOf(18, 20, 21, 23, 25, 28, 30, 32, 35, 38)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserEligibilityProfile,
    onUpdateProfile: (UserEligibilityProfile) -> Unit,
    bookmarkedJobs: List<JobEntity>,
    language: AppLanguage,
    onOpenLanguageSheet: () -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    onOpenJobAlerts: () -> Unit = {},
    onNavigateToJobs: (() -> Unit)? = null,
    documentLocker: AspirantDocumentLocker = AspirantDocumentLocker(),
    onUpdateDocumentLocker: (AspirantDocumentLocker) -> Unit = {},
    appliedExams: List<AppliedExamRecord> = emptyList(),
    onAddAppliedExam: (AppliedExamRecord) -> Unit = {},
    onUpdateExamStage: (String, ExamApplicationStage, String) -> Unit = { _, _, _ -> },
    onDeleteAppliedExam: (String) -> Unit = {},
    onShareDossier: () -> Unit = {},
    backendSyncState: BackendSyncState = BackendSyncState.Idle,
    onRefreshData: () -> Unit = {},
    allJobsCount: Int = 0,
    resultsCount: Int = 0,
    admitCardsCount: Int = 0,
    syllabiCount: Int = 0,
    admissionsCount: Int = 0,
    onOpenAdminAds: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditLockerDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    val profileTabs = listOf(
        "Eligibility & Physical",
        "Document Locker",
        "My Applications (${appliedExams.size})",
        "Saved Jobs (${bookmarkedJobs.size})",
        "Preferences & Export"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 1. Executive Modern Digital Aspirant Passport Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF070D1E),
                                DeepNavy,
                                Color(0xFF162238)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column {
                    // Top identity row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar with gradient halo
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(RoyalBlue, Color(0xFF60A5FA), Color(0xFF818CF8))
                                        )
                                    )
                                    .padding(2.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DeepNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Candidate Avatar",
                                        tint = PureWhite,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userProfile.candidateName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Aspirant",
                                        tint = EmeraldMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Digital Aspirant Command Center",
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Share / Print Dossier Action Button
                        Button(
                            onClick = onShareDossier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalBlue,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_share_dossier")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Dossier",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dossier",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick passport metrics chip bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🎂 ${userProfile.age} Yrs", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                Text(text = " • ${userProfile.category.displayName.substringBefore(" (")}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📏 ${userProfile.heightCm.toInt()} cm", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                Text(text = " • ${userProfile.gender}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🎓 ${userProfile.qualification.displayName.substringBefore(" (")}", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🏛️ ${userProfile.homeState}", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Navigation Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = DeepNavy,
            edgePadding = 14.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DeepNavy,
                        height = 3.dp
                    )
                }
            }
        ) {
            profileTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("profile_tab_$index")
                )
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            // TAB 0: Eligibility & Physical Standards
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card A: Education Level & Stream
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Highest Qualification",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Filters recruitments requiring this degree or below",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                EducationLevel.values().forEach { edu ->
                                    val isSelected = userProfile.qualification == edu
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(qualification = edu)) },
                                        label = {
                                            Text(
                                                text = edu.displayName.substringBefore(" ("),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepNavy,
                                            selectedLabelColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("edu_chip_${edu.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Specific Degree / Branch Stream",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = userProfile.specificDegree,
                                onValueChange = { onUpdateProfile(userProfile.copy(specificDegree = it)) },
                                singleLine = true,
                                placeholder = { Text("e.g. B.Tech Computer Science, B.Sc Physics, B.Com", fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate100,
                                    unfocusedContainerColor = Slate100,
                                    focusedBorderColor = DeepNavy,
                                    unfocusedBorderColor = Slate200
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_specific_degree")
                            )
                        }
                    }

                    // Card B: Caste Category & Age Relaxation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Social Category & Age Relaxation",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Statutory age relaxation auto-applied for job eligibility",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+${userProfile.category.standardRelaxationYears} Yrs",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CasteCategory.values().forEach { cat ->
                                    val isSelected = userProfile.category == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(category = cat)) },
                                        label = {
                                            Text(
                                                text = cat.displayName.substringBefore(" ("),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalBlue,
                                            selectedLabelColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("caste_chip_${cat.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Candidate Age Selector
                            Text(
                                text = "Current Age (${userProfile.age} Years)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAgeOptions.forEach { ageOpt ->
                                    val isSelected = userProfile.age == ageOpt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(age = ageOpt)) },
                                        label = { Text("$ageOpt Yrs", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepNavy,
                                            selectedLabelColor = PureWhite
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Card C: Domicile State Reservation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Domicile / Home State",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Unlocks state reservation quotas",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PopularProfileStates.forEach { st ->
                                    val isSelected = userProfile.homeState == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(homeState = st)) },
                                        label = { Text(st, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalBlue,
                                            selectedLabelColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("state_chip_$st")
                                    )
                                }
                            }
                        }
                    }

                    // Card D: Physical & Medical Standards Evaluator (Police & Defence)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_physical_evaluator_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                            .background(Color(0xFFEEF2FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Physical Standards Checker",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Police, Defence & CAPF Ground Eligibility",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue
                                        )
                                    }
                                }

                                // Gender selector toggle
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Slate100)
                                        .padding(2.dp)
                                ) {
                                    val isMale = userProfile.gender.equals("MALE", ignoreCase = true)
                                    Surface(
                                        onClick = { onUpdateProfile(userProfile.copy(gender = "MALE", isWomenCandidate = false)) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isMale) DeepNavy else Color.Transparent
                                    ) {
                                        Text(
                                            text = "Male",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMale) PureWhite else Slate600,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = { onUpdateProfile(userProfile.copy(gender = "FEMALE", isWomenCandidate = true)) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!isMale) DeepNavy else Color.Transparent
                                    ) {
                                        Text(
                                            text = "Female",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isMale) PureWhite else Slate600,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Height Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Candidate Height: ${userProfile.heightCm.toInt()} cm",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = {
                                            if (userProfile.heightCm > 140f) {
                                                onUpdateProfile(userProfile.copy(heightCm = userProfile.heightCm - 1f))
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("-1 cm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            if (userProfile.heightCm < 210f) {
                                                onUpdateProfile(userProfile.copy(heightCm = userProfile.heightCm + 1f))
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("+1 cm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Slider(
                                value = userProfile.heightCm,
                                onValueChange = { onUpdateProfile(userProfile.copy(heightCm = it)) },
                                valueRange = 140f..205f,
                                steps = 64,
                                colors = SliderDefaults.colors(
                                    thumbColor = RoyalBlue,
                                    activeTrackColor = RoyalBlue,
                                    inactiveTrackColor = Slate200
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Chest measurement (for male candidates)
                            if (userProfile.gender.equals("MALE", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = "${userProfile.chestUnexpandedCm.toInt()}",
                                        onValueChange = {
                                            val v = it.toFloatOrNull() ?: 80f
                                            onUpdateProfile(userProfile.copy(chestUnexpandedCm = v))
                                        },
                                        label = { Text("Chest Normal (cm)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = "${userProfile.chestExpandedCm.toInt()}",
                                        onValueChange = {
                                            val v = it.toFloatOrNull() ?: 85f
                                            onUpdateProfile(userProfile.copy(chestExpandedCm = v))
                                        },
                                        label = { Text("Chest Expanded (cm)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Live Force Benchmark Verifications",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Benchmark List
                            val benchmarks = remember(userProfile.heightCm, userProfile.gender, userProfile.category, userProfile.chestUnexpandedCm, userProfile.chestExpandedCm) {
                                PhysicalStandardsChecker.evaluateAllForces(userProfile)
                            }

                            benchmarks.forEach { bm ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (bm.overallStatus) {
                                        PhysicalEligibilityStatus.PASS -> Color(0xFFF0FDF4)
                                        PhysicalEligibilityStatus.BORDERLINE -> Color(0xFFFFFBEB)
                                        PhysicalEligibilityStatus.BELOW_CUTOFF -> Color(0xFFFEF2F2)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when (bm.overallStatus) {
                                            PhysicalEligibilityStatus.PASS -> Color(0xFFBBF7D0)
                                            PhysicalEligibilityStatus.BORDERLINE -> Color(0xFFFDE68A)
                                            PhysicalEligibilityStatus.BELOW_CUTOFF -> Color(0xFFFECACA)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bm.forceName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = bm.postTitle,
                                                    fontSize = 11.sp,
                                                    color = Slate600
                                                )
                                            }

                                            Surface(
                                                color = Color(bm.overallStatus.colorHex),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = bm.overallStatus.label,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PureWhite,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Required: ${bm.requiredHeightCm} cm",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate700
                                            )
                                            Text(
                                                text = bm.tips,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (bm.overallStatus) {
                                                    PhysicalEligibilityStatus.PASS -> EmeraldGreen
                                                    PhysicalEligibilityStatus.BORDERLINE -> SaffronOrange
                                                    PhysicalEligibilityStatus.BELOW_CUTOFF -> Color(0xFFDC2626)
                                                }
                                            )
                                        }

                                        if (bm.requiredChest != null) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Chest Norm: ${bm.requiredChest}",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Running Test: ${bm.runningTest}",
                                            fontSize = 10.sp,
                                            color = Slate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: Document & Certificate Locker (DigiLocker Style)
            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner with Edit Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(RoyalBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Aspirant Document Vault",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Tap copy icon to quickly paste details in application forms",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }
                            }

                            Button(
                                onClick = { showEditLockerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_edit_document_locker")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 10th Card
                    DocumentLockerItemCard(
                        title = "10th (Matriculation)",
                        institution = documentLocker.tenthBoard,
                        year = documentLocker.tenthYear,
                        percentage = documentLocker.tenthPercentage,
                        primaryIdentifierLabel = "Roll Number",
                        primaryIdentifierValue = documentLocker.tenthRollNo,
                        secondaryIdentifierLabel = "Marksheet No",
                        secondaryIdentifierValue = documentLocker.tenthMarksheetNo,
                        onCopy = { copyToClipboard("10th Roll Number", documentLocker.tenthRollNo) }
                    )

                    // 12th Card
                    DocumentLockerItemCard(
                        title = "12th (Intermediate / 10+2)",
                        institution = "${documentLocker.twelfthBoard} (${documentLocker.twelfthStream})",
                        year = documentLocker.twelfthYear,
                        percentage = documentLocker.twelfthPercentage,
                        primaryIdentifierLabel = "Roll Number",
                        primaryIdentifierValue = documentLocker.twelfthRollNo,
                        secondaryIdentifierLabel = "Certificate No",
                        secondaryIdentifierValue = documentLocker.twelfthCertificateNo,
                        onCopy = { copyToClipboard("12th Roll Number", documentLocker.twelfthRollNo) }
                    )

                    // Graduation Card
                    DocumentLockerItemCard(
                        title = "Graduation Degree",
                        institution = "${documentLocker.gradDegree} • ${documentLocker.gradUniversity}",
                        year = documentLocker.gradYear,
                        percentage = documentLocker.gradCgpaOrPercentage,
                        primaryIdentifierLabel = "Roll / Enrolment No",
                        primaryIdentifierValue = documentLocker.gradRollNo,
                        secondaryIdentifierLabel = "Degree Serial No",
                        secondaryIdentifierValue = documentLocker.gradDegreeSerialNo,
                        onCopy = { copyToClipboard("Graduation Roll No", documentLocker.gradRollNo) }
                    )

                    // Category Certificate Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category / Caste Certificate",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                IconButton(
                                    onClick = { copyToClipboard("Category Certificate Number", documentLocker.categoryCertNo) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "Cert No: ${documentLocker.categoryCertNo}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Issued by: ${documentLocker.categoryIssueAuthority} on ${documentLocker.categoryIssueDate}",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    // Domicile Certificate Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "State Domicile Certificate",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                IconButton(
                                    onClick = { copyToClipboard("Domicile Certificate Number", documentLocker.domicileCertNo) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "Cert No: ${documentLocker.domicileCertNo}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Domicile State: ${documentLocker.domicileState} (Issued: ${documentLocker.domicileIssueDate})",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    // Photo ID Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = documentLocker.idType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text(text = documentLocker.idNumberMasked, fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                            }
                            IconButton(
                                onClick = { copyToClipboard("ID Number", documentLocker.idNumberMasked) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // TAB 2: My Applications & Exam Journey Tracker
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Actions & Count Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "My Applied Recruitments",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = "Track stages from submission to merit list",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Button(
                            onClick = { showAddExamDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue, contentColor = PureWhite),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_log_new_application")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Application", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${appliedExams.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RoyalBlue)
                                Text(text = "Total Applied", fontSize = 10.sp, color = Slate600)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5))
                        ) {
                            val admitCount = appliedExams.count { it.stage == ExamApplicationStage.ADMIT_CARD_OUT }
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$admitCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SaffronOrange)
                                Text(text = "Admit Cards", fontSize = 10.sp, color = Slate600)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                        ) {
                            val qualifiedCount = appliedExams.count { it.stage == ExamApplicationStage.SELECTED }
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$qualifiedCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                                Text(text = "Selected", fontSize = 10.sp, color = Slate600)
                            }
                        }
                    }

                    if (appliedExams.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No exam applications logged yet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                Text("Click '+ Log Application' to store your registration and exam schedule.", fontSize = 11.sp, color = Slate500, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        appliedExams.forEach { exam ->
                            AppliedExamCard(
                                exam = exam,
                                onUpdateStage = { stage, notes -> onUpdateExamStage(exam.id, stage, notes) },
                                onDelete = { onDeleteAppliedExam(exam.id) },
                                onCopyAppNo = { copyToClipboard("Application Number", exam.applicationNo) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    NativeSponsoredJobCard(creativeIndex = 1)
                }
            }

            // TAB 3: Saved Jobs
            3 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Deadline Alerts Active Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlarmOn,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Deadline Reminders Active (${bookmarkedJobs.size} Saved)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "3-day & 24h reminders scheduled for each saved opening.",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    if (bookmarkedJobs.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No saved vacancies yet.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap the bookmark icon on any vacancy card to save it here.", fontSize = 11.sp, color = Slate500)
                            }
                        }
                    } else {
                        bookmarkedJobs.forEach { job ->
                            JobCard(
                                job = job,
                                onClick = { onJobClick(job) },
                                onBookmarkToggle = { onBookmarkToggle(job) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    NativeSponsoredJobCard(creativeIndex = 2)
                }
            }

            // TAB 4: Preferences, Sync & Export
            4 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Feature Highlight: Printable Aspirant Resume Dossier Card
                    Card(
                        onClick = onShareDossier,
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_profile_dossier")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                            .background(DeepNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Aspirant Career Dossier",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DeepNavy
                                            )
                                            Text(
                                                text = "1-Click Printable Summary / PDF Export",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBlue
                                            )
                                        }
                                    }

                                    Surface(
                                        color = EmeraldGreen,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Export",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Generates a clean text/printable summary containing your candidate credentials, document IDs, physical benchmarks, and active applications for fast sharing or printing.",
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Feature Highlight: Targeted Custom Job Alert Preferences Card
                    Card(
                        onClick = onOpenJobAlerts,
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_btn_targeted_alerts")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(DeepNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Targeted Job Alerts", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = DeepNavy)
                                        Text(text = "Multi-Criteria Alert Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    }
                                }
                                Surface(color = EmeraldGreen, shape = RoundedCornerShape(8.dp)) {
                                    Text("Tune", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Filter alerts strictly by target state, degree qualification, preferred departments & minimum vacancies.",
                                fontSize = 12.sp,
                                color = Slate700,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Display Language Settings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(text = "App Preferences", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                onClick = onOpenLanguageSheet,
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_btn_language")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = "Display Language", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                            Text(text = "${language.nativeName} (${language.englishName})", fontSize = 11.sp, color = SaffronOrange, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = Slate500)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Deadline notification toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Deadline Reminder Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    Text(text = "Receive notifications 3 days and 24h prior to closing", fontSize = 11.sp, color = Slate500)
                                }
                                Switch(
                                    checked = userProfile.enableDeadlineAlerts,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(enableDeadlineAlerts = it)) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = DeepNavy)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            // New openings alert toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "New Vacancy Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    Text(text = "Receive notifications when new UPSC, SSC, RRB recruitments are verified", fontSize = 11.sp, color = Slate500)
                                }
                                Switch(
                                    checked = userProfile.enableNewJobAlerts,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(enableNewJobAlerts = it)) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = DeepNavy)
                                )
                            }
                        }
                    }

                    // Modern Backend API & Offline Dual Caching Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_backend_api_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Color(0xFF0284C7), RoyalBlue))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Backend API Sync", tint = PureWhite, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Backend API & Dual Caching", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                        Text(text = "Edge Worker & SQLite Room Layer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    }
                                }

                                Surface(
                                    color = when (backendSyncState) {
                                        is BackendSyncState.Syncing -> Color(0xFFFFEDD5)
                                        is BackendSyncState.Success -> Color(0xFFDCFCE7)
                                        else -> Slate100
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (backendSyncState) {
                                                        is BackendSyncState.Syncing -> SaffronOrange
                                                        is BackendSyncState.Success -> EmeraldGreen
                                                        else -> Slate500
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = when (backendSyncState) {
                                                is BackendSyncState.Syncing -> "Syncing..."
                                                is BackendSyncState.Success -> "Connected"
                                                is BackendSyncState.OfflineCached -> "Cached Mode"
                                                else -> "Ready"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (backendSyncState) {
                                                is BackendSyncState.Syncing -> SaffronOrange
                                                is BackendSyncState.Success -> EmeraldGreen
                                                else -> Slate700
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sync state descriptive message
                            val syncMessage = when (backendSyncState) {
                                is BackendSyncState.Syncing -> backendSyncState.currentEntity
                                is BackendSyncState.Success -> backendSyncState.message
                                is BackendSyncState.OfflineCached -> backendSyncState.message
                                else -> "Local SQLite cache synced with official Cloudflare backend API endpoints."
                            }

                            Surface(shape = RoundedCornerShape(10.dp), color = Slate100, modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = syncMessage, fontSize = 11.sp, color = Slate700, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cache Breakdown Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFEFF6FF), border = BorderStroke(1.dp, Color(0xFFDBEAFE))) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "$allJobsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = RoyalBlue)
                                        Text(text = "Jobs", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFF0FDF4), border = BorderStroke(1.dp, Color(0xFFDCFCE7))) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "$resultsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                                        Text(text = "Results", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF7ED), border = BorderStroke(1.dp, Color(0xFFFFEDD5))) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "$admitCardsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SaffronOrange)
                                        Text(text = "Cards", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFFAF5FF), border = BorderStroke(1.dp, Color(0xFFF3E8FF))) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "$syllabiCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7E22CE))
                                        Text(text = "Syllabus", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFFDF2F8), border = BorderStroke(1.dp, Color(0xFFFCE7F3))) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "$admissionsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBE185D))
                                        Text(text = "Admission", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onRefreshData,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_sync_all_backend_data")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Force Refresh & Sync All Backend Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Statutory Disclaimer & Gazette Integrity
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Editorial & Gazette Transparency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All recruitment circulars, examination schemes, and syllabus PDFs are parsed strictly from official Government Gazette releases (upsc.gov.in, ssc.gov.in, rrbcdg.gov.in).",
                                fontSize = 11.sp,
                                color = Slate600,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Sarkari Jobs App v2.5.0 • Aspirant Command Center 🇮🇳", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        }
                    }
                }
            }
        }
    }

    // Edit Document Vault Dialog
    if (showEditLockerDialog) {
        EditDocumentLockerDialog(
            currentLocker = documentLocker,
            onSave = {
                onUpdateDocumentLocker(it)
                showEditLockerDialog = false
                Toast.makeText(context, "Document Vault updated successfully!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditLockerDialog = false }
        )
    }

    // Add Exam Application Dialog
    if (showAddExamDialog) {
        AddExamApplicationDialog(
            onSave = {
                onAddAppliedExam(it)
                showAddExamDialog = false
                Toast.makeText(context, "Application added to Exam Tracker!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddExamDialog = false }
        )
    }
}

@Composable
private fun DocumentLockerItemCard(
    title: String,
    institution: String,
    year: String,
    percentage: String,
    primaryIdentifierLabel: String,
    primaryIdentifierValue: String,
    secondaryIdentifierLabel: String,
    secondaryIdentifierValue: String,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = institution, fontSize = 11.sp, color = Slate600)
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Roll", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = primaryIdentifierLabel, fontSize = 10.sp, color = Slate500)
                    Text(text = primaryIdentifierValue, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RoyalBlue)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Score / %", fontSize = 10.sp, color = Slate500)
                    Text(text = percentage, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                }
            }

            if (secondaryIdentifierValue.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$secondaryIdentifierLabel: $secondaryIdentifierValue", fontSize = 10.sp, color = Slate500)
                    Text(text = "Passing Year: $year", fontSize = 10.sp, color = Slate500)
                }
            }
        }
    }
}

@Composable
private fun AppliedExamCard(
    exam: AppliedExamRecord,
    onUpdateStage: (ExamApplicationStage, String) -> Unit,
    onDelete: () -> Unit,
    onCopyAppNo: () -> Unit
) {
    var showStageMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(exam.stage.badgeColorHex),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = exam.stage.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        TextButton(
                            onClick = { showStageMenu = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Change Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        }

                        DropdownMenu(
                            expanded = showStageMenu,
                            onDismissRequest = { showStageMenu = false }
                        ) {
                            ExamApplicationStage.values().forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName, fontSize = 12.sp) },
                                    onClick = {
                                        onUpdateStage(st, "")
                                        showStageMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = exam.jobTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(text = "${exam.organization} • Advt: ${exam.advtNumber}", fontSize = 11.sp, color = Slate600)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Application No.", fontSize = 10.sp, color = Slate500)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = exam.applicationNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Slate400,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCopyAppNo() }
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Exam Date / Venue", fontSize = 10.sp, color = Slate500)
                    Text(text = exam.examDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
            }

            if (exam.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${exam.notes}",
                        fontSize = 11.sp,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditDocumentLockerDialog(
    currentLocker: AspirantDocumentLocker,
    onSave: (AspirantDocumentLocker) -> Unit,
    onDismiss: () -> Unit
) {
    var candidateName by remember { mutableStateOf(currentLocker.candidateName) }
    var fatherName by remember { mutableStateOf(currentLocker.fatherName) }
    var dob by remember { mutableStateOf(currentLocker.dob) }
    var tenthRollNo by remember { mutableStateOf(currentLocker.tenthRollNo) }
    var tenthPercentage by remember { mutableStateOf(currentLocker.tenthPercentage) }
    var twelfthRollNo by remember { mutableStateOf(currentLocker.twelfthRollNo) }
    var twelfthPercentage by remember { mutableStateOf(currentLocker.twelfthPercentage) }
    var gradRollNo by remember { mutableStateOf(currentLocker.gradRollNo) }
    var gradPercentage by remember { mutableStateOf(currentLocker.gradCgpaOrPercentage) }
    var categoryCertNo by remember { mutableStateOf(currentLocker.categoryCertNo) }
    var domicileCertNo by remember { mutableStateOf(currentLocker.domicileCertNo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Document Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = candidateName, onValueChange = { candidateName = it }, label = { Text("Candidate Name") }, singleLine = true)
                OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("Father's Name") }, singleLine = true)
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth (DD/MM/YYYY)") }, singleLine = true)
                OutlinedTextField(value = tenthRollNo, onValueChange = { tenthRollNo = it }, label = { Text("10th Roll No.") }, singleLine = true)
                OutlinedTextField(value = tenthPercentage, onValueChange = { tenthPercentage = it }, label = { Text("10th Marks / %") }, singleLine = true)
                OutlinedTextField(value = twelfthRollNo, onValueChange = { twelfthRollNo = it }, label = { Text("12th Roll No.") }, singleLine = true)
                OutlinedTextField(value = twelfthPercentage, onValueChange = { twelfthPercentage = it }, label = { Text("12th Marks / %") }, singleLine = true)
                OutlinedTextField(value = gradRollNo, onValueChange = { gradRollNo = it }, label = { Text("Graduation Roll No.") }, singleLine = true)
                OutlinedTextField(value = gradPercentage, onValueChange = { gradPercentage = it }, label = { Text("Graduation CGPA / %") }, singleLine = true)
                OutlinedTextField(value = categoryCertNo, onValueChange = { categoryCertNo = it }, label = { Text("Category Certificate No.") }, singleLine = true)
                OutlinedTextField(value = domicileCertNo, onValueChange = { domicileCertNo = it }, label = { Text("Domicile Certificate No.") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentLocker.copy(
                            candidateName = candidateName,
                            fatherName = fatherName,
                            dob = dob,
                            tenthRollNo = tenthRollNo,
                            tenthPercentage = tenthPercentage,
                            twelfthRollNo = twelfthRollNo,
                            twelfthPercentage = twelfthPercentage,
                            gradRollNo = gradRollNo,
                            gradCgpaOrPercentage = gradPercentage,
                            categoryCertNo = categoryCertNo,
                            domicileCertNo = domicileCertNo
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite)
            ) {
                Text("Save Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddExamApplicationDialog(
    onSave: (AppliedExamRecord) -> Unit,
    onDismiss: () -> Unit
) {
    var jobTitle by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var applicationNo by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var examCenter by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Application", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = { Text("Post / Exam Name *") }, placeholder = { Text("e.g. SSC CGL 2026") }, singleLine = true)
                OutlinedTextField(value = organization, onValueChange = { organization = it }, label = { Text("Recruitment Board *") }, placeholder = { Text("e.g. SSC / UPPSC") }, singleLine = true)
                OutlinedTextField(value = applicationNo, onValueChange = { applicationNo = it }, label = { Text("Application / Reg. No. *") }, singleLine = true)
                OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll No. (If issued)") }, singleLine = true)
                OutlinedTextField(value = examDate, onValueChange = { examDate = it }, label = { Text("Tentative Exam Date") }, placeholder = { Text("e.g. 24 Dec 2026") }, singleLine = true)
                OutlinedTextField(value = examCenter, onValueChange = { examCenter = it }, label = { Text("Preferred Exam Center") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes / Admit Card details") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jobTitle.isNotBlank() && applicationNo.isNotBlank()) {
                        val record = AppliedExamRecord(
                            id = "app-${System.currentTimeMillis()}",
                            jobTitle = jobTitle,
                            organization = organization.ifBlank { "Government Department" },
                            advtNumber = "Recruitment 2026",
                            applicationNo = applicationNo,
                            rollNo = rollNo,
                            examDate = examDate.ifBlank { "TBA" },
                            examCenter = examCenter.ifBlank { "Home State" },
                            stage = ExamApplicationStage.SUBMITTED,
                            notes = notes
                        )
                        onSave(record)
                    }
                },
                enabled = jobTitle.isNotBlank() && applicationNo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue, contentColor = PureWhite)
            ) {
                Text("Log Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
