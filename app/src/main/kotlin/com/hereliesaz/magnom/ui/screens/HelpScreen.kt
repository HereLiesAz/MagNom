package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Context-aware Help Screen.
 *
 * Displays help information relevant to the screen from which it was invoked.
 *
 * @param route The navigation route of the previous screen.
 */
@Composable
fun HelpScreen(route: String) {
    val helpText = when {
        route.startsWith("parse") -> "This screen allows you to analyze audio recordings of magnetic stripes. Load a WAV file or record audio to visualize the waveform. Use pinch-to-zoom to inspect details. The app attempts to decode the data automatically using F2F decoding."
        route.startsWith("editor") -> "Use this screen to manually edit card profile details. You can also attach photos of the card. The 'Save' button persists changes to secure storage."
        route.startsWith("transmission") -> "This is the emulation interface. Ensure your hardware device (BLE or USB) is connected. Press 'Transmit' to send the track data to the device for spoofing."
        route.startsWith("settings") -> "Configure app preferences here. You can enable backups (password protected) and manage BLE connections."
        route.startsWith("bruteforce") -> "A tool for testing system response to sequential data variations. Use responsibly."
        route == "main" -> "Welcome to MagNom. This is your card wallet. Tap '+' to add a card, or tap an existing card to edit or transmit it."
        else -> "No specific help available for this screen."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = helpText,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
