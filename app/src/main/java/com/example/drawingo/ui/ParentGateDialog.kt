package com.example.drawingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.drawingo.net.AdcBackendClient
import kotlin.random.Random

@Composable
fun ParentGateDialog(
    isKioskEnabled: Boolean,
    isCloudAiAllowed: Boolean,
    currentBackendUrl: String,
    currentScreenTimeLimit: Int,
    onKioskToggled: (Boolean) -> Unit,
    onCloudAiAllowedChanged: (Boolean) -> Unit,
    onBackendUrlSaved: (String) -> Unit,
    onScreenTimeSaved: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val num1 = remember { Random.nextInt(13, 30) }
    val num2 = remember { Random.nextInt(12, 20) }
    val expectedAnswer = num1 * num2

    var isUnlocked by remember { mutableStateOf(false) }
    var parentInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var kioskState by remember { mutableStateOf(isKioskEnabled) }
    var cloudAiState by remember { mutableStateOf(isCloudAiAllowed) }
    var backendUrlInput by remember { mutableStateOf(currentBackendUrl) }
    var screenTimeInput by remember { mutableFloatStateOf(currentScreenTimeLimit.toFloat()) }
    val backendUrlValid = backendUrlInput.isBlank() || AdcBackendClient.isSecureBackendUrl(backendUrlInput.trim())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isUnlocked) {
                    Text(
                        text = "🔒 Parents Only",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Solve this to continue:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "$num1 × $num2 = ?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4FC3F7), // Sky Blue
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = parentInput,
                        onValueChange = {
                            parentInput = it
                            errorMessage = null
                        },
                        label = { Text("Your Answer") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4FC3F7),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedLabelColor = Color(0xFF4FC3F7),
                            unfocusedLabelColor = Color(0xFF9CA3AF),
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        ),
                        singleLine = true
                    )
                    if (!backendUrlValid) {
                        Text(
                            "Use a secure HTTPS server URL.",
                            color = Color(0xFFFF6B9E),
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFF6B9E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            enabled = backendUrlValid,
                            onClick = {
                                if (parentInput.trim().toIntOrNull() == expectedAnswer) {
                                    isUnlocked = true
                                } else {
                                    errorMessage = "Oops! Try again."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)), // Sunny Yellow
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Unlock", color = Color(0xFF4B5563), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                } else {
                    Text(
                        text = "⚙️ Parent Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kiosk Lock Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1F2937),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pins app screen & blocks system navigation until 4-finger exit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = kioskState,
                            onCheckedChange = { kioskState = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4FC3F7), // Sky Blue
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cloud AI drawing analysis", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text(
                                "When enabled, the drawing is sent to Drawingo's server and Google's Gemini AI. Off-device drawing analysis is disabled by default.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = cloudAiState,
                            onCheckedChange = { cloudAiState = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4FC3F7),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ADC Backend Server URL",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = backendUrlInput,
                        onValueChange = { backendUrlInput = it },
                        placeholder = { Text("https://drawingo-backend...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4FC3F7),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Daily Screen Time Limit: ${screenTimeInput.toInt()} mins",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Slider(
                        value = screenTimeInput,
                        onValueChange = { screenTimeInput = it },
                        valueRange = 15f..120f,
                        steps = 6, // 15, 30, 45, 60, 75, 90, 105, 120
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4FC3F7),
                            activeTrackColor = Color(0xFF4FC3F7),
                            inactiveTrackColor = Color(0xFFE5E7EB)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onKioskToggled(kioskState)
                                onCloudAiAllowedChanged(cloudAiState)
                                onBackendUrlSaved(backendUrlInput.trim())
                                onScreenTimeSaved(screenTimeInput.toInt())
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)), // Minty Green
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Save", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
