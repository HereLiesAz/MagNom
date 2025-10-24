package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.ParseViewModel

@Composable
fun SwipeSelectionScreen(
    navController: NavController,
    parseViewModel: ParseViewModel = viewModel()
) {
    val uiState by parseViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        uiState.errorMessage?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
        Text(text = "ZCR Threshold: ${uiState.zcrThreshold}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = uiState.zcrThreshold.toFloat(),
            onValueChange = { parseViewModel.onZcrThresholdChange(it.toDouble()) },
            valueRange = 0f..1f
        )
        Text(text = "Window Size: ${uiState.windowSize}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = uiState.windowSize.toFloat(),
            onValueChange = { parseViewModel.onWindowSizeChange(it.toInt()) },
            valueRange = 256f..4096f
        )
        LazyColumn {
            items(uiState.swipes) { swipe ->
                Text(
                    text = "Swipe from ${swipe.start} to ${swipe.end}",
                    modifier = Modifier.clickable { parseViewModel.onSwipeSelected(swipe) },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(
            onClick = {
                uiState.selectedSwipe?.let {
                    val swipeJson = Gson().toJson(it)
                    navController.navigate(Screen.CreateCardProfile.createRoute(swipeJson))
                }
            },
            enabled = uiState.selectedSwipe != null
        ) {
            Text("Create New Profile", style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = {
                parseViewModel.createTrimmedWavFile(context)
            },
            enabled = uiState.selectedSwipe != null
        ) {
            Text("Create Trimmed Clip", style = MaterialTheme.typography.bodySmall)
        }
        uiState.trimmedFilePath?.let {
            Text("Trimmed file saved to: $it", style = MaterialTheme.typography.bodySmall)
        }
    }
}
