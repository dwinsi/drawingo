package com.example.drawingo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingo.net.GeminiInteractionLogger
import org.json.JSONObject

private enum class LogFilter(val label: String, val icon: String) {
    ALL("All", "📋"),
    ERRORS("Errors & Filtered", "⚠️"),
    VEO("Veo Videos", "🎬"),
    GEMINI("Gemini Scenes", "✨")
}

/**
 * Full-screen page for reviewing and inspecting cloud AI requests,
 * API operations, safety filter diagnostics, and payload responses.
 */
@Composable
fun ApiLogsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var logs by remember { mutableStateOf(GeminiInteractionLogger.readLogs(context).reversed()) }
    var selectedFilter by remember { mutableStateOf(LogFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var copyNotice by remember { mutableStateOf<String?>(null) }

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { entry ->
            val phase = entry.optString("phase", "")
            val details = entry.optJSONObject("details") ?: JSONObject()
            val httpStatus = details.optInt("httpStatus", 0)
            val isError = phase.contains("error") || httpStatus >= 400 || details.has("error")

            val matchesFilter = when (selectedFilter) {
                LogFilter.ALL -> true
                LogFilter.ERRORS -> isError
                LogFilter.VEO -> phase.startsWith("veo_")
                LogFilter.GEMINI -> !phase.startsWith("veo_")
            }

            if (!matchesFilter) return@filter false

            if (searchQuery.isBlank()) true
            else {
                val fullText = entry.toString().lowercase()
                fullText.contains(searchQuery.trim().lowercase())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Navigation & Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF25283A))
                        }
                    }
                    Column {
                        Text(
                            "API & Cloud Logs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF25283A)
                        )
                        Text(
                            "${logs.size} total events recorded on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF656979)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (logs.isNotEmpty()) {
                        Surface(
                            onClick = {
                                val jsonArray = logs.joinToString("\n\n") { it.toString(2) }
                                clipboardManager.setText(AnnotatedString(jsonArray))
                                copyNotice = "All logs copied to clipboard!"
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFEAF5),
                            contentColor = Color(0xFF69489B)
                        ) {
                            Text(
                                "Copy all",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            onClick = { showClearConfirmation = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFECEF),
                            contentColor = Color(0xFFC53C65)
                        ) {
                            Text(
                                "Clear",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search logs by prompt, status, operation ID...") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF25283A),
                    unfocusedTextColor = Color(0xFF25283A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF69489B),
                    unfocusedBorderColor = Color(0xFFE2DDD5),
                    cursorColor = Color(0xFF69489B)
                )
            )

            // Filter Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LogFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        LogFilter.ALL -> logs.size
                        LogFilter.ERRORS -> logs.count {
                            val phase = it.optString("phase", "")
                            val details = it.optJSONObject("details") ?: JSONObject()
                            phase.contains("error") || details.optInt("httpStatus", 0) >= 400 || details.has("error")
                        }
                        LogFilter.VEO -> logs.count { it.optString("phase", "").startsWith("veo_") }
                        LogFilter.GEMINI -> logs.count { !it.optString("phase", "").startsWith("veo_") }
                    }

                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF25283A) else Color.White,
                        contentColor = if (isSelected) Color.White else Color(0xFF555866),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E2D9))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(filter.icon, fontSize = 13.sp)
                            Text(
                                "${filter.label} ($count)",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Copy Toast Notification
            AnimatedVisibility(
                visible = copyNotice != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        copyNotice.orEmpty(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Event Logs List
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Text(
                            if (searchQuery.isNotBlank()) "No logs match \"$searchQuery\""
                            else if (logs.isEmpty()) "No cloud API calls recorded yet"
                            else "No events in this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF454856)
                        )
                        Text(
                            "All Gemini drawing analyses and Veo video generations are automatically logged here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7E8292),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLogs) { logEntry ->
                        LogEventCard(logEntry = logEntry)
                    }
                }
            }
        }

        // Clear Confirmation Dialog
        if (showClearConfirmation) {
            AlertDialog(
                onDismissRequest = { showClearConfirmation = false },
                title = { Text("Clear All Logs?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove all stored API and cloud interaction logs from your device.") },
                confirmButton = {
                    Button(
                        onClick = {
                            GeminiInteractionLogger.clearLogs(context)
                            logs = emptyList()
                            showClearConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53C65))
                    ) { Text("Clear all") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun LogEventCard(logEntry: JSONObject) {
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }

    val timestamp = logEntry.optString("timestamp", "")
    val interactionId = logEntry.optString("interactionId", "")
    val phase = logEntry.optString("phase", "event")
    val details = logEntry.optJSONObject("details") ?: JSONObject()

    val httpStatus = details.optInt("httpStatus", 0)
    val model = details.optString("model", "")
    val prompt = details.optString("prompt", "")
    val aspectRatio = details.optString("aspectRatio", "")
    val operationId = details.optString("operationId", "")
    val errorMessage = details.optString("error", details.optString("message", ""))
    val body = details.optString("body", "")

    val isError = phase.contains("error") || httpStatus >= 400 || errorMessage.isNotBlank()
    val isSuccess = phase.contains("complete") || httpStatus in 200..299

    val badgeColor = when {
        isError -> Color(0xFFB4234D)
        isSuccess -> Color(0xFF1E824C)
        phase.contains("poll") -> Color(0xFF0F75BC)
        else -> Color(0xFF555866)
    }

    val badgeBg = when {
        isError -> Color(0xFFFFECEF)
        isSuccess -> Color(0xFFE8F5E9)
        phase.contains("poll") -> Color(0xFFE1F5FE)
        else -> Color(0xFFF0EFEA)
    }

    val phaseIcon = when {
        phase.startsWith("veo_video_complete") -> "🎉"
        phase.startsWith("veo_video_submit") -> "🚀"
        phase.startsWith("veo_video_poll") -> "⏳"
        phase.startsWith("veo_") -> "🎬"
        phase.contains("error") -> "⚠️"
        else -> "🔮"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Phase + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(phaseIcon, fontSize = 18.sp)
                    Text(
                        phase,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF25283A)
                    )
                }

                if (httpStatus > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeBg,
                        contentColor = badgeColor
                    ) {
                        Text(
                            "HTTP $httpStatus",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Timestamp and Interaction ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7E8292)
                )
                if (interactionId.isNotBlank()) {
                    Text(
                        "id: ${interactionId.take(8)}…",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFA5A8B4)
                    )
                }
            }

            // Metadata Chips (Model, Aspect Ratio)
            if (model.isNotBlank() || aspectRatio.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (model.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF4F0F9),
                            contentColor = Color(0xFF69489B)
                        ) {
                            Text(
                                model,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (aspectRatio.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            contentColor = Color(0xFFE65100)
                        ) {
                            Text(
                                "Ratio: $aspectRatio",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Prompt Snippet
            if (prompt.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF7F5F0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E919C))
                        Text(
                            "\"$prompt\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF25283A)
                        )
                    }
                }
            }

            // Error or Filtered Message Warning Box
            if (errorMessage.isNotBlank() || (httpStatus >= 400 && body.isNotBlank())) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF0F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD4DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Error / Safety Filter Notice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB4234D))
                        Text(
                            if (errorMessage.isNotBlank()) errorMessage else body,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8C1D38)
                        )
                    }
                }
            }

            // Operation Identifier
            if (operationId.isNotBlank()) {
                Text(
                    "Operation: ${operationId.substringAfterLast("/")}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF8E919C)
                )
            }

            // Expand / Collapse Raw JSON Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (isExpanded) "Hide details ▲" else "View full JSON ▼",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69489B)
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(logEntry.toString(2)))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("📋", fontSize = 14.sp)
                }
            }

            // Expanded Full JSON Box
            if (isExpanded) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2230),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        logEntry.toString(2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF98E6FF),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        // e.g., 2026-10-07T19:06:37.828624Z -> 19:06:37 (UTC)
        val timePart = isoString.substringAfter("T").take(8)
        val datePart = isoString.substringBefore("T")
        "$datePart $timePart UTC"
    } catch (e: Exception) {
        isoString
    }
}
