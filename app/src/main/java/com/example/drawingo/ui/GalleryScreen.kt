package com.example.drawingo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Brush
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(
    viewModel: DrawingViewModel,
    onNavigateToCanvas: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsState()
    val context = LocalContext.current
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.createNewProject()
            viewModel.setBackgroundImage(context, it)
            onNavigateToCanvas()
        }
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let {
                viewModel.createNewProject()
                viewModel.setBackgroundImage(context, it)
                onNavigateToCanvas()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { fabMenuExpanded = true },
                    containerColor = com.example.drawingo.theme.LuxGold,
                    contentColor = com.example.drawingo.theme.ObsidianBlack,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New Drawing")
                }
                DropdownMenu(
                    expanded = fabMenuExpanded,
                    onDismissRequest = { fabMenuExpanded = false },
                    modifier = Modifier.background(com.example.drawingo.theme.MidnightIndigo)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Blank Canvas", color = Color.White) },
                        leadingIcon = { Icon(Icons.Rounded.Brush, contentDescription = null, tint = Color.White) },
                        onClick = {
                            fabMenuExpanded = false
                            viewModel.createNewProject()
                            onNavigateToCanvas()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import from Camera", color = Color.White) },
                        leadingIcon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White) },
                        onClick = {
                            fabMenuExpanded = false
                            val tempFile = File.createTempFile("camera_temp", ".jpg", File(context.cacheDir, "camera_images").apply { mkdirs() })
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import from Gallery", color = Color.White) },
                        leadingIcon = { Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White) },
                        onClick = {
                            fabMenuExpanded = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            }
        },
        containerColor = com.example.drawingo.theme.ObsidianBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            Text(
                text = "My Drawings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.drawingo.theme.LuxGold,
                modifier = Modifier.padding(24.dp)
            )

            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No drawings yet. Tap + to start!", color = com.example.drawingo.theme.MutedSlate)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(projects) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadProject(project.id)
                                    onNavigateToCanvas()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.drawingo.theme.MidnightIndigo),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(com.example.drawingo.theme.IndigoGloss),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Brush, contentDescription = null, tint = com.example.drawingo.theme.SoftGold, modifier = Modifier.size(48.dp))
                                }
                                Text(
                                    text = project.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(Date(project.updatedAt)),
                                    fontSize = 12.sp,
                                    color = com.example.drawingo.theme.MutedSlate,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
