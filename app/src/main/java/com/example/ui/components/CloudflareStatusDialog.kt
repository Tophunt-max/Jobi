package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.repository.CloudflareSyncState
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun CloudflareStatusDialog(
    currentWorkerUrl: String,
    syncState: CloudflareSyncState,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    var editedUrl by remember(currentWorkerUrl) { mutableStateOf(currentWorkerUrl) }
    var showUrlEditor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF97316).copy(alpha = 0.15f), // Cloudflare Orange tint
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Cloudflare Edge Backend",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Workers API • D1 SQL • R2 Storage",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live Status Badge
                Surface(
                    color = when (syncState) {
                        is CloudflareSyncState.Synced -> GreenContainer
                        is CloudflareSyncState.Syncing -> Color(0xFFFEF3C7)
                        is CloudflareSyncState.Error -> Color(0xFFFEE2E2)
                        else -> Slate100
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (syncState) {
                            is CloudflareSyncState.Syncing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Connecting to Edge...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = "Querying Cloudflare D1 tables",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                            is CloudflareSyncState.Synced -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = OnGreenContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cloudflare Edge Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnGreenContainer
                                    )
                                    Text(
                                        text = "${syncState.jobCount} live records synced with Room cache",
                                        fontSize = 11.sp,
                                        color = Slate700
                                    )
                                }
                            }
                            is CloudflareSyncState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Room Offline Cache Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = "Operating safely on local SQLite database",
                                        fontSize = 11.sp,
                                        color = Slate700
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = DeepNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Edge Ready",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Tap Sync Now to fetch latest jobs",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Highlights
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloudflare D1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("SQL at Edge", fontSize = 11.sp, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloudflare R2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Zero Egress Bandwidth", fontSize = 11.sp, color = EmeraldMint, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Worker Endpoint Configuration
                if (showUrlEditor) {
                    OutlinedTextField(
                        value = editedUrl,
                        onValueChange = { editedUrl = it },
                        label = { Text("Worker URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cloudflare_url_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showUrlEditor = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSaveUrl(editedUrl)
                                showUrlEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                        ) {
                            Text("Save & Connect")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Worker Endpoint:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate500
                            )
                            Text(
                                text = currentWorkerUrl,
                                fontSize = 12.sp,
                                color = DeepNavy,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = { showUrlEditor = true },
                            modifier = Modifier.testTag("edit_worker_url_button")
                        ) {
                            Icon(Icons.Default.Link, contentDescription = "Edit URL", tint = DeepNavy)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSyncNow,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("cloudflare_sync_now_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
