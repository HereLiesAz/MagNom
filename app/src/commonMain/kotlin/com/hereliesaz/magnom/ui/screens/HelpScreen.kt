package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class HelpTopic(val title: String, val body: String)

private val topics = listOf(
    HelpTopic(
        "Cards",
        "The home screen lists your saved cards. Tap New card to add one from readable fields, or use " +
            "Analyze to enter raw track data. Each card can be transmitted, edited, or deleted. Track 1 and " +
            "Track 2 are generated and LRC-checked automatically — a card can't be saved with invalid data.",
    ),
    HelpTopic(
        "Analyze",
        "The raw data analyzer decodes Track 1 / Track 2 strings as you type, showing LRC/format validity " +
            "and the decoded PAN, expiry and service code. Valid tracks can be saved directly as a card.",
    ),
    HelpTopic(
        "Transmit",
        "Choose a transport and emulate the swipe. Audio plays the F2F waveform through the audio output " +
            "(works anywhere). USB drives a MagSpoof v4/v5 over CDC-ACM serial. Bluetooth sends the same " +
            "serial protocol to a paired MagSpoof/HC-05-class device. Pair Bluetooth devices in system settings first.",
    ),
    HelpTopic(
        "Security & backups",
        "Enable App lock in Settings to require biometric/device credential. All data is encrypted on-device " +
            "and never sent to a server. Backups are separately encrypted with a password you choose — keep it " +
            "safe, as backups can't be restored without it.",
    ),
)

@Composable
fun HelpScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Help", style = MaterialTheme.typography.headlineSmall)
        topics.forEach { topic ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(topic.title, style = MaterialTheme.typography.titleMedium)
                    Text(topic.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
