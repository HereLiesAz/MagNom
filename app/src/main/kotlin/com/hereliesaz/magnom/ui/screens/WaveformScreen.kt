package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.WaveformViewModel
import com.hereliesaz.magnom.viewmodels.WaveformViewModelFactory

@Composable
fun WaveformScreen(
    navController: NavController,
    cardId: String,
    waveformViewModel: WaveformViewModel = viewModel(
        factory = WaveformViewModelFactory(
            LocalContext.current.applicationContext as Application,
            cardId
        )
    )
) {
    val uiState by waveformViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { waveformViewModel.generateWaveform() }) {
            Text("Generate Waveform")
        }
        Button(onClick = { waveformViewModel.playWaveform() }) {
            Text("Play")
        }

        uiState.waveformData?.let { data ->
            val density = LocalDensity.current
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = with(density) { 12.sp.toPx() }
            }

            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        waveformViewModel.onZoom(zoom)
                        waveformViewModel.onPan(pan.x)
                    }
                }
            ) {
                val step = (size.width / data.size) * uiState.zoom
                val start = (uiState.panOffset / step).toInt().coerceIn(0, data.size)
                val end = (start + (size.width / step).toInt()).coerceIn(0, data.size)

                for (i in start until end - 1) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = (i - start) * step, y = (size.height / 2) - (data[i] * size.height / 2)),
                        end = Offset(x = (i + 1 - start) * step, y = (size.height / 2) - (data[i + 1] * size.height / 2))
                    )
                }

                uiState.trackData?.let { trackData ->
                    val charWidth = step * 5 // 5 bits per character
                    for (i in trackData.indices) {
                        val x = i * charWidth - uiState.panOffset
                        if (x > -charWidth && x < size.width) {
                            drawIntoCanvas {
                                it.nativeCanvas.drawText(
                                    trackData[i].toString(),
                                    x,
                                    size.height - 20.dp.toPx(),
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
