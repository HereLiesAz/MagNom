package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardRepository
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

    init {
        loadWaveformData()
    }

    private fun loadWaveformData() {
        viewModelScope.launch {
            val card = cardRepository.getCardById(cardId)
            val track2 = card?.track2
            if (track2 != null) {
                // This is a placeholder for the actual waveform data
                _uiState.value = _uiState.value.copy(waveformData = List(100) { (it % 2).toFloat() }, trackData = track2)
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
