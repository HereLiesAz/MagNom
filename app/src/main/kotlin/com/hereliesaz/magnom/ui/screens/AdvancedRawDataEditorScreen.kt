package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.AdvancedRawDataEditorViewModel

@Composable
fun AdvancedRawDataEditorScreen(navController: NavController, viewModel: AdvancedRawDataEditorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        TextField(
            value = uiState.rawTrack1,
            onValueChange = viewModel::onTrack1Change,
            label = { Text("Raw Track 1 Data") },
            textStyle = MaterialTheme.typography.bodySmall
        )
        TextField(
            value = uiState.rawTrack2,
            onValueChange = viewModel::onTrack2Change,
            label = { Text("Raw Track 2 Data") },
            textStyle = MaterialTheme.typography.bodySmall,
            isError = !uiState.isTrack2Valid
        )

        if (uiState.isTrack2Valid && uiState.parsedTrack2Data != null) {
            Text("Parsed Track 2 Data:", style = MaterialTheme.typography.bodySmall)
            Text("PAN: ${uiState.parsedTrack2Data?.pan}", style = MaterialTheme.typography.bodySmall)
            Text("Expiration: ${uiState.parsedTrack2Data?.expirationDate}", style = MaterialTheme.typography.bodySmall)
            Text("Service Code: ${uiState.parsedTrack2Data?.serviceCode}", style = MaterialTheme.typography.bodySmall)
        } else if (!uiState.isTrack2Valid) {
            Text("Invalid Track 2 Data", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
    }
}
