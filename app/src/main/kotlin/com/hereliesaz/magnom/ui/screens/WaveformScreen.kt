package com.hereliesaz.magnom.ui.screens

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.viewmodels.WaveformViewModel
import com.hereliesaz.magnom.viewmodels.WaveformViewModelFactory

@Composable
fun WaveformScreen(
    navController: NavController,
    cardId: String
) {
    val context = LocalContext.current
    val cardRepository = CardRepository(context.applicationContext as Application)
    val waveformViewModel: WaveformViewModel = viewModel(
        factory = WaveformViewModelFactory(cardRepository, cardId)
    )

    val uiState by waveformViewModel.uiState.collectAsState()
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = { waveformViewModel.togglePlayback() }) {
            Text(if (uiState.isPlaying) "Stop" else "Play")
        }

        uiState.waveformData?.let { data ->
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        waveformViewModel.onZoom(zoom)
                        val maxPan = (size.width * uiState.zoom) - size.width
                        val newPan = (uiState.panOffset + pan.x).coerceIn(0f, maxPan)
                        waveformViewModel.setPan(newPan)
                    }
                }
            ) {
                if (data.isEmpty()) return@Canvas

                val step = (size.width / data.size) * uiState.zoom
                if (step == 0f) return@Canvas

                val start = (uiState.panOffset / step).toInt().coerceIn(0, data.size)
                val end = (start + (size.width / step).toInt()).coerceIn(0, data.size)

                for (i in start until end - 1) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = (i - start) * step, y = (size.height / 2) - (data[i] * size.height / 2)),
                        end = Offset(x = (i + 1 - start) * step, y = (size.height / 2) - (data[i + 1] * size.height / 2))
                    )
                }

                val trackData = uiState.trackData ?: ""
                val charStep = step * 5 // 5 bits per character for Track 2
                for (i in trackData.indices) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = trackData[i].toString(),
                        style = TextStyle(color = Color.Black),
                        topLeft = Offset(x = i * charStep - uiState.panOffset, y = size.height - 50)
                    )
                }
            }
        }
    }
}
