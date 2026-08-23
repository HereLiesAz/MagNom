package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.hereliesaz.magnom.ui.CardEditorViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardEditorScreen(cardId: String?, onDone: () -> Unit) {
    val vm: CardEditorViewModel = koinViewModel()
    LaunchedEffect(cardId) { vm.load(cardId) }
    val s by vm.state.collectAsState()

    LaunchedEffect(s.saved) { if (s.saved) onDone() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (cardId == null) "New Card" else "Edit Card", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(s.label, vm::onLabel, label = { Text("Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            s.pan, vm::onPan, label = { Text("PAN (card number)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(s.name, vm::onName, label = { Text("Cardholder name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            s.expiration, vm::onExpiration, label = { Text("Expiration (YYMM)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            s.serviceCode, vm::onServiceCode, label = { Text("Service code (3 digits)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )

        s.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Text(
            "Track 1 and Track 2 are generated and LRC-checked on save. A card cannot be saved with invalid data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) { Text("Save card") }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}
