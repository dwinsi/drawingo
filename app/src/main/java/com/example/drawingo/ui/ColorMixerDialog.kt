package com.example.drawingo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import com.example.drawingo.theme.StudioAccent
import com.example.drawingo.theme.StudioControl
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText

@Composable
fun ColorMixerDialog(
    initialColor: Color,
    onColorMixed: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }
    var hexInput by remember(initialColor) { mutableStateOf(initialColor.toHexString()) }
    val normalizedHex = hexInput.trim().removePrefix("#")
    val hexColor = normalizedHex.takeIf { it.matches(Regex("[0-9A-Fa-f]{6}")) }
        ?.let { runCatching { Color(android.graphics.Color.parseColor("#$it")) }.getOrNull() }
    val previewColor = hexColor ?: Color(red, green, blue)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = StudioSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Custom color",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = StudioText
                )

                Text(
                    "Adjust the sliders or enter a hex value.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioMutedText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorSwatch("Current", initialColor, Modifier.weight(1f))
                    ColorSwatch("Preview", previewColor, Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { value ->
                        val validCharacters = value.all { it == '#' || it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        val hasSingleLeadingPrefix = value.count { it == '#' } <= 1 && (!value.contains('#') || value.startsWith('#'))
                        if (validCharacters && hasSingleLeadingPrefix && value.length <= 7) {
                            hexInput = value.uppercase()
                            val parsed = value.trim().removePrefix("#")
                                .takeIf { it.matches(Regex("[0-9A-Fa-f]{6}")) }
                                ?.let { runCatching { Color(android.graphics.Color.parseColor("#$it")) }.getOrNull() }
                            if (parsed != null) {
                                red = parsed.red
                                green = parsed.green
                                blue = parsed.blue
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("HEX") },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    isError = hexInput.isNotBlank() && hexColor == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    supportingText = {
                        if (hexInput.isNotBlank() && hexColor == null) Text("Enter six hex digits, for example #C53C65")
                    }
                )
                
                // Sliders
                ColorSlider("Red", red) { red = it; hexInput = Color(it, green, blue).toHexString() }
                ColorSlider("Green", green) { green = it; hexInput = Color(red, it, blue).toHexString() }
                ColorSlider("Blue", blue) { blue = it; hexInput = Color(red, green, it).toHexString() }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = StudioText)
                    ) { Text("Cancel") }
                    Button(
                        onClick = { onColorMixed(hexColor ?: Color(red, green, blue)) },
                        enabled = hexInput.isBlank() || hexColor != null,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAccent)
                    ) { Text("Apply") }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = StudioMutedText)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .border(1.dp, StudioOutline, RoundedCornerShape(12.dp))
        )
    }
}

private fun Color.toHexString(): String = "#%06X".format(0xFFFFFF and toArgb())

@Composable
private fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioMutedText)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = StudioAccent,
                activeTrackColor = StudioAccent.copy(alpha = 0.72f),
                inactiveTrackColor = StudioControl
            )
        )
    }
}
