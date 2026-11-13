package com.example.drawingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.drawingo.theme.DrawingoThemeMode
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.StudioControl
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText
import com.example.drawingo.ui.DrawingViewModel

@Composable
fun TopKeepBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onHideMenuClick: () -> Unit,
    onZenModeClick: () -> Unit = {},
    onLayersClick: () -> Unit,
    onPaperStyleClick: () -> Unit,
    onSaveEditableProject: () -> Unit = {},
    onExportClick: (String) -> Unit,
    onGalleryClick: () -> Unit,
    viewModel: DrawingViewModel,
    modifier: Modifier = Modifier,
    themeMode: DrawingoThemeMode = DrawingoThemeMode.SYSTEM,
    onThemeModeChange: (DrawingoThemeMode) -> Unit = {},
    toolCaddyOnLeft: Boolean = false,
    onToggleToolCaddySide: () -> Unit = {}
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    val currentProjectId by viewModel.currentProjectId.collectAsState()
    val saveStatus by viewModel.projectSaveStatus.collectAsState()

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(24.dp))
            .background(StudioSurface)
            .border(1.dp, StudioOutline, RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactAction(Icons.Rounded.Close, "Hide menus", size = 40.dp, onClick = onHideMenuClick)
        CompactAction(Icons.Rounded.Image, "Gallery", size = 40.dp, onClick = onGalleryClick)
        CompactAction(Icons.AutoMirrored.Rounded.Undo, "Undo", enabled = canUndo, size = 40.dp, onClick = onUndo)
        CompactAction(Icons.AutoMirrored.Rounded.Redo, "Redo", enabled = canRedo, size = 40.dp, onClick = onRedo)
        CompactAction(Icons.Rounded.Delete, "Clear drawing", size = 40.dp, onClick = { showClearConfirmation = true })
        Box {
            CompactAction(Icons.Rounded.Save, "Save & Export", size = 40.dp, onClick = { actionsExpanded = true })
            DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Save editable project") },
                    leadingIcon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                    onClick = { actionsExpanded = false; onSaveEditableProject() }
                )
                DropdownMenuItem(
                    text = { Text("Save as PNG") },
                    onClick = { actionsExpanded = false; onExportClick("PNG") }
                )
                DropdownMenuItem(
                    text = { Text("Save as JPEG") },
                    onClick = { actionsExpanded = false; onExportClick("JPEG") }
                )
                DropdownMenuItem(
                    text = { Text("Appearance: ${themeMode.label}") },
                    onClick = {
                        actionsExpanded = false
                        showAppearanceDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move tool caddy to ${if (toolCaddyOnLeft) "right" else "left"}") },
                    leadingIcon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
                    onClick = {
                        actionsExpanded = false
                        onToggleToolCaddySide()
                    }
                )
            }
        }
        if (currentProjectId != null || saveStatus != DrawingViewModel.ProjectSaveStatus.SAVED) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (saveStatus) {
                    DrawingViewModel.ProjectSaveStatus.SAVING -> CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = StudioAccentUi,
                        strokeWidth = 2.dp
                    )
                    DrawingViewModel.ProjectSaveStatus.SAVED -> Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = StudioAccentUi,
                        modifier = Modifier.size(15.dp)
                    )
                    DrawingViewModel.ProjectSaveStatus.FAILED -> Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFB3261E),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = when (saveStatus) {
                        DrawingViewModel.ProjectSaveStatus.SAVING -> "Saving"
                        DrawingViewModel.ProjectSaveStatus.SAVED -> "Saved"
                        DrawingViewModel.ProjectSaveStatus.FAILED -> "Save failed"
                    },
                    color = if (saveStatus == DrawingViewModel.ProjectSaveStatus.FAILED) Color(0xFFB3261E) else StudioMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DrawingoThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onThemeModeChange(mode)
                                    showAppearanceDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    onThemeModeChange(mode)
                                    showAppearanceDialog = false
                                }
                            )
                            Text(mode.label, color = StudioText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppearanceDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = StudioSurface,
            titleContentColor = StudioText,
            textContentColor = StudioText
        )
    }

    if (showClearConfirmation) {
        Dialog(onDismissRequest = { showClearConfirmation = false }) {
            Surface(
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(24.dp),
                color = StudioSurface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Start a fresh picture?",
                        color = StudioText,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Your drawing will be cleared from the canvas.",
                        color = StudioMutedText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showClearConfirmation = false },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioControl,
                                contentColor = StudioText
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text("Keep drawing", fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = {
                                showClearConfirmation = false
                                onClear()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioAccentUi,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text("Clear drawing", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    size: Dp = 44.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(when { selected -> StudioAccentSoft; enabled -> StudioControl; else -> StudioControl.copy(alpha = 0.6f) })
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when { selected -> StudioAccentUi; enabled -> StudioText; else -> StudioMutedText },
            modifier = Modifier.size(20.dp)
        )
    }
}
