package com.hereliesaz.magnom.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Waveform : Screen("waveform/{cardId}") {
        fun createRoute(cardId: String) = "waveform/$cardId"
    }
    object AudioFileSelection : Screen("audio_file_selection")
    object SwipeSelection : Screen("swipe_selection")
    object AdvancedEditor : Screen("advanced_editor")
    object Settings : Screen("settings")
    object Editor : Screen("editor")
    object Transmission : Screen("transmission/{cardId}") {
        fun createRoute(cardId: String) = "transmission/$cardId"
    }
    object CreateCardProfile : Screen("create_card_profile/{swipeData}") {
        fun createRoute(swipeData: String) = "create_card_profile/$swipeData"
    }
}
