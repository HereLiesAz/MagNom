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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.hereliesaz.magnom.ui.RawAnalyzerViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Advanced Raw Data Editor from the original blueprint — finally reachable, and now with
 * the live LRC/parse feedback it always promised.
 */
@Composable
fun RawAnalyzerScreen(onDone: () -> Unit) {
    val vm: RawAnalyzerViewModel = koinViewModel()
    val s by vm.state.collectAsState()
    LaunchedEffect(s.saved) { if (s.saved) onDone() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Raw Data Analyzer", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(s.label, vm::onLabel, label = { Text("Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            s.track1, vm::onTrack1, label = { Text("Track 1") }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            isError = s.track1.isNotEmpty() && !s.track1Valid, modifier = Modifier.fillMaxWidth(),
        )
        ValidityLine(s.track1.isNotEmpty(), s.track1Valid, "Track 1")

        OutlinedTextField(
            s.track2, vm::onTrack2, label = { Text("Track 2") }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            isError = s.track2.isNotEmpty() && !s.track2Valid, modifier = Modifier.fillMaxWidth(),
        )
        ValidityLine(s.track2.isNotEmpty(), s.track2Valid, "Track 2")

        s.decoded?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Button(
            onClick = vm::save,
            enabled = s.track1Valid && s.track2Valid,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save as card") }
    }
}

@Composable
private fun ValidityLine(present: Boolean, valid: Boolean, label: String) {
    if (!present) return
    Text(
        if (valid) "$label: valid — LRC checks" else "$label: invalid — LRC or format mismatch",
        style = MaterialTheme.typography.bodySmall,
        color = if (valid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
    )
}
