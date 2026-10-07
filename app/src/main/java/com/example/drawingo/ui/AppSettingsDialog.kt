package com.example.drawingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawingo.net.AdcBackendClient

@Composable
fun AppSettingsDialog(
    cloudAiEnabled: Boolean,
    backendUrl: String,
    onCloudAiEnabledChanged: (Boolean) -> Unit,
    onBackendUrlSaved: (String) -> Unit,
    onOpenApiLogs: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var cloudEnabled by remember { mutableStateOf(cloudAiEnabled) }
    var url by remember { mutableStateOf(backendUrl) }
    val validUrl = url.isBlank() || AdcBackendClient.isSecureBackendUrl(url.trim())

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f).widthIn(max = 560.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color(0xFF25283A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF25283A))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloud AI features", style = MaterialTheme.typography.titleMedium, color = Color(0xFF25283A))
                        Text(
                            "When enabled, a drawing you choose to analyze or turn into a video is sent to your configured HTTPS backend and Google Cloud. Video generation is available only when the backend owner enables it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF656979)
                        )
                    }
                    Switch(checked = cloudEnabled, onCheckedChange = { cloudEnabled = it })
                }
                Text("Backend server URL", style = MaterialTheme.typography.titleMedium, color = Color(0xFF25283A))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://your-backend.example") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF25283A),
                        unfocusedTextColor = Color(0xFF25283A),
                        cursorColor = Color(0xFFC53C65),
                        focusedBorderColor = Color(0xFFC53C65),
                        unfocusedBorderColor = Color(0xFF8E919C),
                        focusedPlaceholderColor = Color(0xFF8E919C),
                        unfocusedPlaceholderColor = Color(0xFF8E919C)
                    )
                )
                if (!validUrl) Text("Use a secure HTTPS server URL.", color = Color(0xFFB4234D), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onDismiss()
                        onOpenApiLogs()
                    }) {
                        Text("📋 View API & Cloud logs", color = Color(0xFF69489B), style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        enabled = validUrl,
                        onClick = {
                            onCloudAiEnabledChanged(cloudEnabled)
                            onBackendUrlSaved(url.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53C65))
                    ) { Text("Save") }
                }
            }
        }
    }
}

