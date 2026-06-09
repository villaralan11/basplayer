package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val currentTrack by viewModel.currentTrack.collectAsState()
                val context = LocalContext.current

                // Launcher for native Storage + Notification Permissions (API 33+ or older)
                val permissionsToRequest = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO]
                        ?: permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: false
                    if (audioGranted) {
                        Toast.makeText(context, "Acceso a música concedido. Escaneando...", Toast.LENGTH_SHORT).show()
                        viewModel.scanAndLoadSongs()
                    } else {
                        Toast.makeText(context, "Permiso denegado. Usando pistas demo de alta resolución.", Toast.LENGTH_LONG).show()
                    }
                }

                // Auto-prompt permission on first launch (friendly UX)
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "library"

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Dynamic morphing Liquid Glass canvas covering background
                    LiquidGlassBackground(currentTrack = currentTrack)

                    // 2. Playback screens over beautiful glass layers
                    Scaffold(
                        containerColor = Color.Transparent, // Let liquid glass show through
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .padding(horizontal = 16.dp, vertical = 2.dp)
                            ) {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth().testTag("bottom_nav_bar"),
                                    shapeValue = RoundedCornerShape(28.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tab 0: Library
                                        NavTabItem(
                                            selected = currentRoute == "library",
                                            activeIcon = Icons.Filled.LibraryMusic,
                                            inactiveIcon = Icons.Outlined.LibraryMusic,
                                            label = "Biblioteca",
                                            onClick = {
                                                navController.navigate("library") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            modifier = Modifier.testTag("nav_tab_library")
                                        )

                                        // Tab 1: Now Playing
                                        NavTabItem(
                                            selected = currentRoute == "playing",
                                            activeIcon = Icons.Filled.PlayCircle,
                                            inactiveIcon = Icons.Outlined.PlayCircle,
                                            label = "Reproduciendo",
                                            onClick = {
                                                navController.navigate("playing") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            modifier = Modifier.testTag("nav_tab_player")
                                        )

                                        // Tab 2: Precision Equalizer
                                        NavTabItem(
                                            selected = currentRoute == "equalizer",
                                            activeIcon = Icons.Filled.Equalizer,
                                            inactiveIcon = Icons.Outlined.Equalizer,
                                            label = "Ecualizador",
                                            onClick = {
                                                navController.navigate("equalizer") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            modifier = Modifier.testTag("nav_tab_equalizer")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "library",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("library") {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            composable("playing") {
                                NowPlayingScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            composable("equalizer") {
                                EqualizerScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavTabItem(
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (selected) 1f else 0.5f,
        animationSpec = spring(),
        label = "tab_opacity"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp)
            .graphicsLayer { alpha = alphaAnim }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = if (selected) Color(0xFFFFD700) else Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) Color(0xFFFFD700) else Color.White,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

