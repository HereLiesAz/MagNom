package com.hereliesaz.magnom.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun CreateCardProfileScreen(
    navController: NavController,
    swipeData: String
) {
    CardEditorScreen(navController = navController)
}
