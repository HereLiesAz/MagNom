package com.hereliesaz.magnom.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Screen for creating a new card profile.
 *
 * Currently reuses the CardEditorScreen, pre-populating it with any available data.
 *
 * @param swipeData JSON string containing the swipe region information (if originating from audio parse).
 */
@Composable
fun CreateCardProfileScreen(
    navController: NavController,
    swipeData: String
) {
    // Reusing the editor UI for creation
    CardEditorScreen(navController = navController)
}
