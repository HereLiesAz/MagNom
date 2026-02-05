package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioParser
import com.hereliesaz.magnom.data.Swipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * UI State for Audio File processing.
 */
data class AudioFileUiState(
    val selectedFileUri: Uri? = null,
    val swipes: List<Swipe> = emptyList(),
    val selectedSwipe: Swipe? = null,
    val errorMessage: String? = null,
    val zcrThreshold: Double = 0.1,
    val windowSize: Int = 1024,
    val trimmedFilePath: String? = null
)

/**
 * ViewModel for loading and analyzing audio files.
 *
 * (Note: This logic is partially duplicated in ParseViewModel, which seems to be the newer/combined implementation.
 * Maintaining this for backward compatibility or separation of concerns if used.)
 */
class AudioFileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AudioFileUiState())
    val uiState: StateFlow<AudioFileUiState> = _uiState

    private var _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri

    fun onFileSelected(context: Context, uri: Uri) {
        _selectedFileUri.value = uri
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val audioData = AudioParser.readWavFile(inputStream)
                    val swipes = AudioParser.findSwipes(
                        audioData.first,
                        _uiState.value.zcrThreshold,
                        _uiState.value.windowSize
                    )
                    _uiState.value = _uiState.value.copy(
                        swipes = swipes,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    swipes = emptyList(),
                    errorMessage = "Failed to parse audio file: ${e.message}"
                )
            }
        }
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
        val currentSwipe = _uiState.value.selectedSwipe ?: return
        val currentUri = _selectedFileUri.value ?: return

        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(currentUri)?.use { inputStream ->
                    val audioData = AudioParser.readWavFile(inputStream)
                    val trimmedData = audioData.first.sliceArray(currentSwipe.start..currentSwipe.end)
                    val outputFile = AudioParser.createWavFile(trimmedData, audioData.second)
                    _uiState.value = _uiState.value.copy(
                        trimmedFilePath = outputFile.absolutePath,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    trimmedFilePath = null,
                    errorMessage = "Failed to create trimmed file: ${e.message}"
                )
            }
        }
    }
}
