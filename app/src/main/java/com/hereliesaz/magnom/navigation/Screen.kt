package com.hereliesaz.magnom.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Editor : Screen("editor")
    object AdvancedEditor : Screen("advanced_editor")
    object Settings : Screen("settings")
}
