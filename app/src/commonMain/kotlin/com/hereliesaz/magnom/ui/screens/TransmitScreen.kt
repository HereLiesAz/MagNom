package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hereliesaz.magnom.ui.TransmitViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransmitScreen(cardId: String, onBack: () -> Unit) {
    val vm: TransmitViewModel = koinViewModel()
    LaunchedEffect(cardId) { vm.load(cardId) }
    val s by vm.state.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Transmit", style = MaterialTheme.typography.headlineSmall)
        val card = s.card
        if (card == null) {
            Text("Card not found.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onBack) { Text("Back") }
            return
        }

        Text(card.label, style = MaterialTheme.typography.titleMedium)
        Text(
            "•••• ${card.pan.takeLast(4)}   exp ${card.expiration}",
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text("Transport", style = MaterialTheme.typography.titleMedium)
        if (vm.available.isEmpty()) {
            Text("No transport available on this device.", color = MaterialTheme.colorScheme.error)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.available.forEach { tx ->
                FilterChip(
                    selected = s.selected == tx.kind,
                    onClick = { vm.select(tx.kind) },
                    label = { Text(tx.kind.name) },
                )
            }
        }

        Button(
            onClick = vm::transmit,
            enabled = !s.busy && s.selected != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Emulate swipe") }

        if (s.busy) CircularProgressIndicator()
        s.message?.let {
            Text(
                it,
                color = if (it.startsWith("Failed")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            )
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
