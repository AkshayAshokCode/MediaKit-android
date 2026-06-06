package com.akshayashokcode.sample_app.ui

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("image",    "Image",    Icons.Filled.Image),
    Tab("media",    "Media",    Icons.Filled.PermMedia),
    Tab("compress", "Compress", Icons.Filled.Compress),
    Tab("record",   "Record",   Icons.Filled.Mic),
    Tab("preview",  "Preview",  Icons.Filled.PlayCircle)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLaunchRecorder: () -> Unit,
    recordingUri: Uri?,
    recordingDurationMs: Long,
    onLaunchCropPicker: () -> Unit,
    croppedUri: Uri?
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    var showVersions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentLabel = tabs.find { it.route == currentRoute }?.label ?: tabs.first().label

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentLabel, fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { showVersions = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Module versions")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "image",
            modifier = Modifier.consumeWindowInsets(innerPadding)
        ) {
            composable("image") {
                CropperScreen(
                    onLaunchCropPicker = onLaunchCropPicker,
                    croppedUri = croppedUri,
                    contentPadding = innerPadding
                )
            }
            composable("media") {
                MediaPickerScreen(contentPadding = innerPadding)
            }
            composable("compress") {
                CompressScreen(contentPadding = innerPadding)
            }
            composable("record") {
                AudioRecorderScreen(
                    onLaunchRecorder = onLaunchRecorder,
                    recordingUri = recordingUri,
                    recordingDurationMs = recordingDurationMs,
                    contentPadding = innerPadding
                )
            }
            composable("preview") {
                MediaPreviewerScreen(contentPadding = innerPadding)
            }
        }
    }

    if (showVersions) {
        ModalBottomSheet(
            onDismissRequest = { showVersions = false },
            sheetState = sheetState
        ) {
            VersionsSheet()
        }
    }
}
