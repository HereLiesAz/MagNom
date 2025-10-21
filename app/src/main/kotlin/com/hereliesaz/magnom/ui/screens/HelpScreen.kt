package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hereliesaz.magnom.navigation.Screen

@Composable
fun HelpScreen(route: String) {
    val helpText = when {
        route.startsWith("main") -> "This is the main screen. Here you can see a list of your saved card profiles. Tap on a profile to view its details, or tap the '+' button to create a new one."
        route.startsWith("parse") -> "This is the parse screen. You can either select an audio file to parse for magnetic stripe data, or you can record your own audio. Once a file is selected or recorded, you can select a swipe to create a new card profile from."
        route.startsWith("editor") -> "This is the card editor screen. Here you can edit the details of a card profile. All changes are saved automatically."
        else -> "This is the help screen. You can find information about how to use the app here."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Help",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = helpText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
