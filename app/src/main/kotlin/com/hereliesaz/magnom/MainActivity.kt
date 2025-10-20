package com.hereliesaz.magnom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.ui.screens.AudioFileSelectionScreen
import com.hereliesaz.magnom.ui.screens.CreateCardProfileScreen
import com.hereliesaz.magnom.ui.screens.MainScreen
import com.hereliesaz.magnom.ui.screens.SwipeSelectionScreen
import com.hereliesaz.magnom.ui.screens.WaveformScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import com.hereliesaz.magnom.viewmodels.AudioFileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagNomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val audioFileViewModel: AudioFileViewModel = viewModel()
                    NavHost(navController = navController, startDestination = Screen.Main.route) {
                        composable(Screen.Main.route) {
                            MainScreen(navController = navController)
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
                        composable(Screen.CreateCardProfile.route) { backStackEntry ->
                            val swipeData = backStackEntry.arguments?.getString("swipeData")
                            if (swipeData != null) {
                                CreateCardProfileScreen(navController = navController, swipeData = swipeData)
                            }
                        }
                    }
                }
            }
        }
    }
}
