package magnom.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Editor : Screen("editor")
    object AdvancedEditor : Screen("advanced_editor")
    object Settings : Screen("settings")
    object Transmission : Screen("transmission/{cardId}") {
        fun createRoute(cardId: String) = "transmission/$cardId"
    }
}
