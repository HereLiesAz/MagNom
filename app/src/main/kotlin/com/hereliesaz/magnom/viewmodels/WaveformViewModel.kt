package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.logic.AudioPlayer
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.logic.WaveformDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WaveformState(
    val waveformData: FloatArray? = null,
    val zoom: Float = 1f,
    val panOffset: Float = 0f,
    val trackData: String? = null,
    val isPlaying: Boolean = false
)

class WaveformViewModel(private val cardRepository: CardRepository, private val cardId: String) : ViewModel() {

    private val waveformDataGenerator = WaveformDataGenerator()
    private val trackDataGenerator = TrackDataGenerator()
    private val audioPlayer = AudioPlayer()

    private val _uiState = MutableStateFlow(WaveformState())
    val uiState: StateFlow<WaveformState> = _uiState.asStateFlow()

    init {
        loadCardData()
    }

    private fun loadCardData() {
        viewModelScope.launch {
            val card = cardRepository.getCardProfile(cardId)
            card?.let {
                val track2 = trackDataGenerator.generateTrack2(it.pan, it.expirationDate, it.serviceCode)
                _uiState.value = _uiState.value.copy(
                    waveformData = waveformDataGenerator.generate(track2),
                    trackData = track2
                )
            }
        }
    }

    fun onZoom(zoomFactor: Float) {
        val newZoom = (_uiState.value.zoom * zoomFactor).coerceIn(0.1f, 10f)
        _uiState.value = _uiState.value.copy(zoom = newZoom)
    }

    fun setPan(pan: Float) {
        _uiState.value = _uiState.value.copy(panOffset = pan)
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            audioPlayer.stop()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            _uiState.value.waveformData?.let {
                audioPlayer.play(it)
                _uiState.value = _uiState.value.copy(isPlaying = true)
            }
        }
    }
}
