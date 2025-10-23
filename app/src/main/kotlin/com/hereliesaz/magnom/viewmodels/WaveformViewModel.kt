package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.logic.WaveformDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WaveformUiState(
    val waveformData: List<Float>? = null,
    val isPlaying: Boolean = false,
    val zoom: Float = 1f,
    val panOffset: Float = 0f,
    val trackData: String? = null
)

class WaveformViewModel(private val cardRepository: CardRepository, private val cardId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(WaveformUiState())
    val uiState: StateFlow<WaveformUiState> = _uiState.asStateFlow()
    private val trackDataGenerator = TrackDataGenerator()
    private val waveformDataGenerator = WaveformDataGenerator()

    init {
        loadWaveformData()
    }

    private fun loadWaveformData() {
        viewModelScope.launch {
            val card = cardRepository.getCardProfile(cardId)
            if (card != null) {
                val track2 = trackDataGenerator.generateTrack2(card.pan, card.expirationDate, card.serviceCode)
                _uiState.value = _uiState.value.copy(
                    waveformData = waveformDataGenerator.generate(track2).toList(),
                    trackData = track2
                )
            }
        }
    }

    fun togglePlayback() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun onZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoom = _uiState.value.zoom * zoom)
    }

    fun setPan(pan: Float) {
        _uiState.value = _uiState.value.copy(panOffset = pan)
    }
}
