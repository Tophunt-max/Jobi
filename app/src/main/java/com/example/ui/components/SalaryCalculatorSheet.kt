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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import com.example.ads.NativeSponsoredJobCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GreenContainer
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

data class PayLevelInfo(
    val levelNumber: Int,
    val name: String,
    val basicPay: Int,
    val typicalPosts: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryCalculatorSheet(
    initialLevel: Int = 7,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val payLevels = listOf(
        PayLevelInfo(1, "Level 1", 18000, "MTS, Group D, Peon"),
        PayLevelInfo(2, "Level 2", 19900, "LDC, Junior Clerk, Constable"),
        PayLevelInfo(3, "Level 3", 21700, "CISF / BSF Constable, Postal DAK"),
        PayLevelInfo(4, "Level 4", 25500, "UDC, Head Constable, Postal Assistant"),
        PayLevelInfo(5, "Level 5", 29200, "Senior Clerk, Auditor Assistant"),
        PayLevelInfo(6, "Level 6", 35400, "Sub-Inspector (SI), Junior Engineer"),
        PayLevelInfo(7, "Level 7", 44900, "ASO (CSS/MEA), Inspector (Income Tax/GST)"),
        PayLevelInfo(8, "Level 8", 47600, "Assistant Audit Officer (AAO)"),
        PayLevelInfo(10, "Level 10", 56100, "IAS, IPS, IRS, Assistant Director, Scientist")
    )

    var selectedLevelNumber by remember { mutableIntStateOf(initialLevel) }
    var selectedCityTier by remember { mutableStateOf("X") } // X: 27%, Y: 18%, Z: 9%

    val currentLevelInfo = payLevels.firstOrNull { it.levelNumber == selectedLevelNumber }
        ?: payLevels[6]

    // Salary Calculations under 7th CPC (Current DA = 50%)
    val salaryCalculation by remember(currentLevelInfo, selectedCityTier) {
        derivedStateOf {
            val basic = currentLevelInfo.basicPay
            val daRate = 0.50 // 50% DA
            val da = (basic * daRate).toInt()

            val hraPercent = when (selectedCityTier) {
                "X" -> 0.27
                "Y" -> 0.18
                else -> 0.09
            }
            val hra = (basic * hraPercent).toInt()

            val baseTa = if (currentLevelInfo.levelNumber >= 9) {
                if (selectedCityTier == "X") 7200 else 3600
            } else if (currentLevelInfo.levelNumber >= 3) {
                if (selectedCityTier == "X") 3600 else 1800
            } else {
                if (selectedCityTier == "X") 1350 else 900
            }
            val ta = baseTa + (baseTa * daRate).toInt() // TA + DA on TA

            val gross = basic + da + hra + ta

            // Deductions
            val nps = ((basic + da) * 0.10).toInt() // 10% NPS deduction
            val cghs = when {
                currentLevelInfo.levelNumber >= 10 -> 1000
                currentLevelInfo.levelNumber >= 7 -> 650
                currentLevelInfo.levelNumber >= 4 -> 450
                else -> 250
            }
            val profTax = 200
            val totalDeductions = nps + cghs + profTax

            val netInHand = gross - totalDeductions

            object {
                val basicPay = basic
                val dearnessAllowance = da
                val houseRentAllowance = hra
                val transportAllowance = ta
                val grossSalary = gross
                val npsDeduction = nps
                val cghsDeduction = cghs
                val deductionsTotal = totalDeductions
                val inHandSalary = netInHand
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
                .testTag("salary_calculator_sheet"),
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
                            color = GreenContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "7th Pay Commission Salary Calc",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Calculated with 50% DA + HRA + TA breakdown",
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

            // Big In-Hand Salary Result Card
            item {
                Surface(
                    color = DeepNavy,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTIMATED MONTHLY IN-HAND SALARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite.copy(alpha = 0.7f)
                            )
                            Surface(
                                color = PureWhite.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "City Tier $selectedCityTier",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "₹${"%,d".format(salaryCalculation.inHandSalary)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )

                        Text(
                            text = "Gross: ₹${"%,d".format(salaryCalculation.grossSalary)} /mo • Deductions: ₹${"%,d".format(salaryCalculation.deductionsTotal)}",
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Select Pay Level
            item {
                Column {
                    Text(
                        text = "1. Select 7th CPC Pay Level",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payLevels) { level ->
                            val isSelected = selectedLevelNumber == level.levelNumber
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLevelNumber = level.levelNumber },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = level.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "₹${"%,d".format(level.basicPay)}", fontSize = 10.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                    Text(
                        text = "Typical Posts: ${currentLevelInfo.typicalPosts}",
                        fontSize = 11.sp,
                        color = Slate600,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Select City Classification (HRA)
            item {
                Column {
                    Text(
                        text = "2. City Posting Tier (HRA Classification)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tier X
                        val isX = selectedCityTier == "X"
                        Card(
                            onClick = { selectedCityTier = "X" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isX) RoyalBlueContainer else Slate100
                            ),
                            border = if (isX) BorderStroke(1.5.dp, RoyalBlue) else null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Tier X (27% HRA)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepNavy)
                                Text(text = "Delhi, Mumbai, Bengaluru, etc.", fontSize = 9.sp, color = Slate600)
                            }
                        }

                        // Tier Y
                        val isY = selectedCityTier == "Y"
                        Card(
                            onClick = { selectedCityTier = "Y" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isY) RoyalBlueContainer else Slate100
                            ),
                            border = if (isY) BorderStroke(1.5.dp, RoyalBlue) else null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Tier Y (18% HRA)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepNavy)
                                Text(text = "State Capitals & Tier-2 Cities", fontSize = 9.sp, color = Slate600)
                            }
                        }

                        // Tier Z
                        val isZ = selectedCityTier == "Z"
                        Card(
                            onClick = { selectedCityTier = "Z" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isZ) RoyalBlueContainer else Slate100
                            ),
                            border = if (isZ) BorderStroke(1.5.dp, RoyalBlue) else null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Tier Z (9% HRA)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepNavy)
                                Text(text = "Rural & Other Towns", fontSize = 9.sp, color = Slate600)
                            }
                        }
                    }
                }
            }

            // Detailed Breakdown Table
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Monthly Salary Itemized Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SalaryItemRow(label = "Basic Pay (Entry Pay)", value = "₹${"%,d".format(salaryCalculation.basicPay)}")
                        SalaryItemRow(label = "Dearness Allowance (DA @ 50%)", value = "₹${"%,d".format(salaryCalculation.dearnessAllowance)}")
                        SalaryItemRow(label = "House Rent Allowance (HRA)", value = "₹${"%,d".format(salaryCalculation.houseRentAllowance)}")
                        SalaryItemRow(label = "Transport Allowance (TA + DA)", value = "₹${"%,d".format(salaryCalculation.transportAllowance)}")

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                        Spacer(modifier = Modifier.height(6.dp))

                        SalaryItemRow(label = "Gross Monthly Earnings", value = "₹${"%,d".format(salaryCalculation.grossSalary)}", isBold = true)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Mandatory Deductions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SalaryItemRow(label = "NPS (10% of Basic + DA)", value = "- ₹${"%,d".format(salaryCalculation.npsDeduction)}", isDeduction = true)
                        SalaryItemRow(label = "CGHS (Medical Scheme)", value = "- ₹${"%,d".format(salaryCalculation.cghsDeduction)}", isDeduction = true)
                        SalaryItemRow(label = "Professional Tax (Approx)", value = "- ₹200", isDeduction = true)

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                        Spacer(modifier = Modifier.height(6.dp))

                        SalaryItemRow(
                            label = "Net In-Hand (Credited to Bank)",
                            value = "₹${"%,d".format(salaryCalculation.inHandSalary)}",
                            isBold = true,
                            highlightColor = EmeraldGreen
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                NativeSponsoredJobCard(creativeIndex = 1)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SalaryItemRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isDeduction: Boolean = false,
    highlightColor: Color = Slate900
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDeduction) Color(0xFF991B1B) else Slate700
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isDeduction) Color(0xFFDC2626) else highlightColor
        )
    }
}
