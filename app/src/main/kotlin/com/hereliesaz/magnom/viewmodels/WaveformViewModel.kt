package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.logic.WaveformDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WaveformState(
    val waveformData: FloatArray? = null,
    val zoom: Float = 1f,
    val panOffset: Float = 0f
)

class WaveformViewModel : ViewModel() {

    private val waveformDataGenerator = WaveformDataGenerator()
    private val trackDataGenerator = TrackDataGenerator()

    private val _uiState = MutableStateFlow(WaveformState())
    val uiState: StateFlow<WaveformState> = _uiState.asStateFlow()

    fun generateWaveform() {
        // For now, we'll use a hardcoded test card.
        val testTrack2 = trackDataGenerator.generateTrack2("1234567890123456", "1234", "123")
        _uiState.value = WaveformState(waveformData = waveformDataGenerator.generate(testTrack2))
    }

    fun onZoom(zoomFactor: Float) {
        val newZoom = (_uiState.value.zoom * zoomFactor).coerceIn(0.1f, 10f)
        _uiState.value = _uiState.value.copy(zoom = newZoom)
    }

    fun onPan(panDelta: Float) {
        val newPanOffset = _uiState.value.panOffset + panDelta
        _uiState.value = _uiState.value.copy(panOffset = newPanOffset)
    }
}
