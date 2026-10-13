package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobEntity
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ReportDialog(
    job: JobEntity,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Outdated Application Dates") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "Outdated Application Dates",
        "Broken or Inaccessible Official Link",
        "Incorrect Vacancy or Pay Scale",
        "Eligibility Criteria Discrepancy",
        "Other Factual Error"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Discrepancy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Advt: ${job.advtNumber} (${job.organization})",
                    fontSize = 12.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Select Issue Type:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(6.dp))

                reasons.forEach { r ->
                    val isSelected = r == selectedReason
                    Surface(
                        onClick = { selectedReason = r },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DeepNavy else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = r,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PureWhite else Slate800,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = { Text("Describe the issue or provide correct official link...", fontSize = 11.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, details) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_report_confirm_btn")
            ) {
                Text("Submit Report", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", fontSize = 12.sp)
            }
        },
        containerColor = PureWhite,
        shape = RoundedCornerShape(16.dp)
    )
}
