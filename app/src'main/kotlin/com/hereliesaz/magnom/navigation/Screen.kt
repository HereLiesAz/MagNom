package com.hereliesaz.magnom.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Parse : Screen("parse/{cardId}") {
        fun createRoute(cardId: String) = "parse/$cardId"
    }
    object SwipeSelection : Screen("swipe_selection")
    object AdvancedEditor : Screen("advanced_editor")
    object Settings : Screen("settings")
    object Editor : Screen("editor/{cardId}") {
        fun createRoute(cardId: String?) = "editor/$cardId"
    }
    object Transmission : Screen("transmission/{cardId}") {
        fun createRoute(cardId: String) = "transmission/$cardId"
    }
    object CreateCardProfile : Screen("create_card_profile/{swipeData}") {
        fun createRoute(swipeData: String) = "create_card_profile/$swipeData"
    }
    object MagspoofReplay : Screen("magspoof_replay")

    object CardSelection : Screen("card_selection")
}
