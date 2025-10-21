package com.hereliesaz.magnom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.aznavrail.AzNavRail
import com.hereliesaz.aznavrail.model.AzButtonShape
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.ui.screens.AudioFileSelectionScreen
import com.hereliesaz.magnom.ui.screens.AudioRecordingScreen
import com.hereliesaz.magnom.ui.screens.CreateCardProfileScreen
import com.hereliesaz.magnom.ui.screens.CardSelectionScreen
import com.hereliesaz.magnom.ui.screens.MainScreen
import com.hereliesaz.magnom.ui.screens.SwipeSelectionScreen
import com.hereliesaz.magnom.ui.screens.WaveformScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import com.hereliesaz.magnom.viewmodels.AudioFileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagNomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val audioFileViewModel: AudioFileViewModel = viewModel()

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                        Row(modifier = Modifier.weight(1f)) {
                            AzNavRail {
                                azRailItem(id = "main", text = "Main", screenTitle = "Card Profiles" ) {
                                    navController.navigate(Screen.Main.route)
                                }
                                azRailItem(id = "audio_file_selection", text = "Parse") {
                                    navController.navigate(Screen.AudioFileSelection.route)
                                }
                                azRailItem(id = "audio_recording", text = "Record", shape = AzButtonShape.NONE ) {
                                    navController.navigate(Screen.AudioRecording.route)
                                }
                                azRailItem(id = "waveform", text = "Waveform", shape = AzButtonShape.NONE ) {
                                    navController.navigate(Screen.CardSelection.route)
                                }
                                azRailItem(id = "magspoof_replay", text = "Replay", shape = AzButtonShape.NONE ) {
                                navController.navigate(Screen.MagspoofReplay.route)
                            }
                                azRailItem(id = "advanced_editor", text = "Advanced") {
                                    navController.navigate(Screen.AdvancedEditor.route)
                                }
                                azMenuItem(id = "settings", text = "Settings") {
                                    navController.navigate(Screen.Settings.route)
                                }
                            }
                            NavHost(navController = navController, startDestination = Screen.Main.route) {
                                composable(Screen.Main.route) {
                                    MainScreen(navController = navController)
                                }
                                composable(Screen.CardSelection.route) {
                                    CardSelectionScreen(navController = navController)
                                }
                                composable(Screen.Waveform.route) { backStackEntry ->
                                    val cardId = backStackEntry.arguments?.getString("cardId")
                                    if (cardId != null) {
                                        WaveformScreen(navController = navController, cardId = cardId)
                                    }
                                }
                                composable(Screen.AudioFileSelection.route) {
                                    AudioFileSelectionScreen(navController = navController, audioFileViewModel = audioFileViewModel)
                                }
                                composable(Screen.SwipeSelection.route) {
                                    SwipeSelectionScreen(navController = navController, audioFileViewModel = audioFileViewModel)
                                }
                                composable(Screen.AudioRecording.route) {
                                    AudioRecordingScreen(navController = navController, audioFileViewModel = audioFileViewModel)
                                }
                                composable(Screen.CreateCardProfile.route) { backStackEntry ->
                                    val swipeData = backStackEntry.arguments?.getString("swipeData")
                                    if (swipeData != null) {
                                        CreateCardProfileScreen(navController = navController, swipeData = swipeData)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}
