package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

        NotesEditor(
            notes = s.notes,
            onAdd = vm::addNote,
            onRemove = vm::removeNote,
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

@Composable
private fun NotesEditor(
    notes: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Notes", style = MaterialTheme.typography.titleMedium)
        notes.forEachIndexed { index, note ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(note, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { onRemove(index) }) { Text("Remove") }
            }
        }
        var draft by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Add a note") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { if (draft.isNotBlank()) { onAdd(draft.trim()); draft = "" } },
                enabled = draft.isNotBlank(),
            ) { Text("Add") }
        }
    }
}
