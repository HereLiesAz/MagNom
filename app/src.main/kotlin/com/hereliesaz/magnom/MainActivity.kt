package com.hereliesaz.magnom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.ui.screens.AdvancedEditorScreen
import com.hereliesaz.magnom.ui.screens.MainScreen
import com.hereliesaz.magnom.ui.screens.SettingsScreen
import com.hereliesaz.magnom.ui.screens.WaveformScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagNomTheme {
                MagNomNavHost()
            }
        }
    }
}

@Composable
fun MagNomNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.AdvancedEditor.route) {
            AdvancedEditorScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Waveform.route) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")
            if (cardId != null) {
                WaveformScreen(navController = navController, cardId = cardId)
            }
        }
    }
}
