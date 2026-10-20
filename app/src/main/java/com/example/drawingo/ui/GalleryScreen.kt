package com.example.drawingo.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.drawingo.ui.DrawingViewModel
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioControl
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    containerColor = StudioAccentUi,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create a drawing")
                }
                DropdownMenu(
                    expanded = fabMenuExpanded,
                    onDismissRequest = { fabMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Blank canvas") },
                        leadingIcon = { Icon(Icons.Rounded.Brush, contentDescription = null) },
                        onClick = {
                            fabMenuExpanded = false
                            viewModel.createNewProject()
                            onNavigateToCanvas()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Take a reference photo") },
                        leadingIcon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null) },
                        onClick = {
                            fabMenuExpanded = false
                            val cameraDirectory = File(context.cacheDir, "camera_images").apply { mkdirs() }
                            val imageFile = File.createTempFile("drawing-reference-", ".jpg", cameraDirectory)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Choose a reference image") },
                        leadingIcon = { Icon(Icons.Rounded.Image, contentDescription = null) },
                        onClick = {
                            fabMenuExpanded = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            }
        },
        containerColor = StudioSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DRAWINGO STUDIO",
                        color = StudioAccentUi,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "Your drawings",
                        color = StudioText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (projects.isNotEmpty()) {
                    Text(
                        text = "${projects.size} ${if (projects.size == 1) "drawing" else "drawings"}",
                        color = StudioMutedText,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(StudioSurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.createNewProject()
                                onNavigateToCanvas()
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = BorderStroke(1.dp, StudioOutline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(StudioAccentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Brush,
                                    contentDescription = null,
                                    tint = StudioAccentUi,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(Modifier.height(22.dp))
                            Text(
                                text = "A little space to create",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioText
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Start with a blank canvas, or bring a reference image into your studio.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioMutedText
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "Tap to start drawing",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = StudioAccentUi
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadProject(project.id)
                                    onNavigateToCanvas()
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurface),
                            border = BorderStroke(1.dp, StudioOutline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                ProjectThumbnail(
                                    path = project.thumbnailPath,
                                    description = "Preview of ${project.name}"
                                )
                                Column(
                                    modifier = Modifier.padding(start = 4.dp, top = 14.dp, end = 4.dp, bottom = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = project.name,
                                        maxLines = 1,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioText,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Edited ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(project.updatedAt))}",
                                        fontSize = 12.sp,
                                        color = StudioMutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectThumbnail(path: String?, description: String) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = path) {
        value = if (path.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(path)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(13.dp))
            .background(StudioControl)
            .padding(8.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Rounded.Brush,
                contentDescription = null,
                tint = StudioMutedText,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
