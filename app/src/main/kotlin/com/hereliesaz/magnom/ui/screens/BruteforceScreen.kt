package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hereliesaz.magnom.viewmodels.BruteforceViewModel

@Composable
fun BruteforceScreen(bruteforceViewModel: BruteforceViewModel = viewModel()) {
    val uiState by bruteforceViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        Text(text = "Bruteforce", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        TextField(
            value = uiState.target,
            onValueChange = bruteforceViewModel::onTargetChange,
            label = { Text("Target") },
            textStyle = MaterialTheme.typography.bodySmall
        )
        TextField(
            value = uiState.charset,
            onValueChange = bruteforceViewModel::onCharsetChange,
            label = { Text("Character Set") },
            textStyle = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = {
                if (uiState.isRunning) {
                    bruteforceViewModel.stopBruteforce()
                } else {
                    bruteforceViewModel.startBruteforce()
                }
            }
        ) {
            Text(if (uiState.isRunning) "Stop" else "Start", style = MaterialTheme.typography.bodySmall)
        }
        Text(text = "Current Attempt: ${uiState.currentAttempt}", style = MaterialTheme.typography.bodySmall)
    }
}
