package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.AudioFileViewModel

@Composable
fun SwipeSelectionScreen(
    navController: NavController,
    audioFileViewModel: AudioFileViewModel = viewModel()
) {
    val swipes by audioFileViewModel.swipes.collectAsState()
    val selectedSwipe by audioFileViewModel.selectedSwipe.collectAsState()
    val errorMessage by audioFileViewModel.errorMessage.collectAsState()
    val zcrThreshold by audioFileViewModel.zcrThreshold.collectAsState()
    val windowSize by audioFileViewModel.windowSize.collectAsState()

    Column {
        errorMessage?.let {
            Text(text = it)
        }
        Text(text = "ZCR Threshold: $zcrThreshold")
        Slider(
            value = zcrThreshold.toFloat(),
            onValueChange = { audioFileViewModel.onZcrThresholdChange(it.toDouble()) },
            valueRange = 0f..1f
        )
        Text(text = "Window Size: $windowSize")
        Slider(
            value = windowSize.toFloat(),
            onValueChange = { audioFileViewModel.onWindowSizeChange(it.toInt()) },
            valueRange = 256f..4096f
        )
        LazyColumn {
            items(swipes) { swipe ->
                Text(
                    text = "Swipe from ${swipe.startTime} to ${swipe.endTime}",
                    modifier = Modifier.clickable { audioFileViewModel.onSwipeSelected(swipe) }
                )
            }
        }
        Button(
            onClick = {
                selectedSwipe?.let {
                    val swipeJson = Gson().toJson(it)
                    navController.navigate(Screen.CreateCardProfile.createRoute(swipeJson))
                }
            },
            enabled = selectedSwipe != null
        ) {
            Text("Create New Profile")
        }
    }
}
