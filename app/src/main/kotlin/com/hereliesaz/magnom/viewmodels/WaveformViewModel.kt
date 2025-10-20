package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.logic.TrackDataGenerator
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.logic.WaveformDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WaveformState(
    val waveformData: FloatArray? = null,
    val trackData: String? = null,
    val zoom: Float = 1f,
    val panOffset: Float = 0f
)

class WaveformViewModel(application: Application, private val cardId: String) : AndroidViewModel(application) {

    private val waveformDataGenerator = WaveformDataGenerator()
    private val cardRepository = CardRepository(application)
    private val trackDataGenerator = TrackDataGenerator()

    private val _uiState = MutableStateFlow(WaveformState())
    val uiState: StateFlow<WaveformState> = _uiState.asStateFlow()

    fun generateWaveform() {
        cardRepository.getCardProfile(cardId)?.let {
            val track2 = trackDataGenerator.generateTrack2(it.pan, it.expirationDate, it.serviceCode)
            _uiState.value = WaveformState(
                waveformData = waveformDataGenerator.generate(track2),
                trackData = track2
            )
        }
    }

    fun onZoom(zoomFactor: Float) {
        val newZoom = (_uiState.value.zoom * zoomFactor).coerceIn(0.1f, 10f)
        _uiState.value = _uiState.value.copy(zoom = newZoom)
    }

    fun onPan(panDelta: Float) {
        val newPanOffset = _uiState.value.panOffset + panDelta
        _uiState.value = _uiState.value.copy(panOffset = newPanOffset)
    }

    fun playWaveform() {
        _uiState.value.waveformData?.let {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(it.size * 4)
                .build()
            audioTrack.write(it, 0, it.size, AudioTrack.WRITE_BLOCKING)
            audioTrack.play()
        }
    }
}
