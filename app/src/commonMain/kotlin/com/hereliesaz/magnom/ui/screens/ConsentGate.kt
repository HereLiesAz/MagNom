package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * First-run ethical-use gate. Unlike the previous build — where the equivalent dialog was
 * dead code that never appeared — this actually blocks the app until acknowledged.
 */
@Composable
fun ConsentGate(onAccept: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("MagNom", style = MaterialTheme.typography.headlineSmall)
        Text("Research & developer tool", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            "MagNom is for analysing and emulating magnetic stripe data on cards you own or are " +
                "authorised to use, for research and development. Using it to capture, clone, or use " +
                "another party's card data is illegal, and you are solely responsible for how you use it.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "All card data stays encrypted on this device. MagNom sends nothing to any server.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) { Text("I understand and accept") }
    }
}
