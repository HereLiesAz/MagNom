package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.media.AudioDeviceInfo
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Swipe(val start: Int, val end: Int)

data class ParseScreenState(
    val isPlaying: Boolean = false,
    val waveformData: List<Float>? = null,
    val zoom: Float = 1f,
    val panOffset: Float = 0f,
    val trackData: String? = null,
    val selectedFileUri: Uri? = null,
    val errorMessage: String? = null,
    val audioData: ShortArray = shortArrayOf(),
    val availableDevices: List<AudioDeviceInfo> = emptyList(),
    val selectedDevice: AudioDeviceInfo? = null,
    val isRecording: Boolean = false,
    val savedFilePath: String? = null,
    val zcrThreshold: Double = 0.2,
    val windowSize: Int = 1024,
    val swipes: List<Swipe> = emptyList(),
    val selectedSwipe: Swipe? = null,
    val trimmedFilePath: String? = null
)

class ParseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ParseScreenState())
    val uiState: StateFlow<ParseScreenState> = _uiState

    fun togglePlayback() {
        _uiState.value = _uiState.value.copy(isPlaying = !uiState.value.isPlaying)
    }

    fun onZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoom = _uiState.value.zoom * zoom)
    }

    fun setPan(pan: Float) {
        _uiState.value = _uiState.value.copy(panOffset = pan)
    }

    fun onFileSelected(context: Context, uri: Uri) {
        // To be implemented
    }

    fun getAvailableRecordingDevices(context: Context) {
        // To be implemented
    }

    fun onDeviceSelected(device: AudioDeviceInfo) {
        _uiState.value = _uiState.value.copy(selectedDevice = device)
    }

    fun startRecording(context: Context, device: AudioDeviceInfo?) {
        // To be implemented
    }

    fun stopRecording() {
        // To be implemented
    }

    fun onZcrThresholdChange(threshold: Double) {
        _uiState.value = _uiState.value.copy(zcrThreshold = threshold)
    }

    fun onWindowSizeChange(size: Int) {
        _uiState.value = _uiState.value.copy(windowSize = size)
    }

    fun onSwipeSelected(swipe: Swipe) {
        _uiState.value = _uiState.value.copy(selectedSwipe = swipe)
    }

    fun createTrimmedWavFile(context: Context) {
        // To be implemented
    }
}
