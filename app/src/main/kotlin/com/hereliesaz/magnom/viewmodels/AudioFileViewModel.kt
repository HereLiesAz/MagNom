package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioParser
import com.hereliesaz.magnom.audio.Swipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioFileViewModel : ViewModel() {

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri.asStateFlow()

    private val _swipes = MutableStateFlow<List<Swipe>>(emptyList())
    val swipes: StateFlow<List<Swipe>> = _swipes.asStateFlow()

    private val _selectedSwipe = MutableStateFlow<Swipe?>(null)
    val selectedSwipe: StateFlow<Swipe?> = _selectedSwipe.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _zcrThreshold = MutableStateFlow(0.1)
    val zcrThreshold: StateFlow<Double> = _zcrThreshold.asStateFlow()

    private val _windowSize = MutableStateFlow(1024)
    val windowSize: StateFlow<Int> = _windowSize.asStateFlow()

    private val _trimmedFilePath = MutableStateFlow<String?>(null)
    val trimmedFilePath: StateFlow<String?> = _trimmedFilePath.asStateFlow()

    private var audioData: ShortArray? = null

    fun onFileSelected(context: Context, uri: Uri) {
        _selectedFileUri.value = uri
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)!!
                val tempFile = File.createTempFile("audio", ".wav", context.cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.use { it.copyTo(outputStream) }
                }
                val (data, _) = AudioParser.readWavFile(tempFile)
                audioData = data
                findSwipes()
            } catch (e: Exception) {
                _errorMessage.value = "Error parsing WAV file: ${e.message}"
            }
        }
    }

    fun onZcrThresholdChange(threshold: Double) {
        _zcrThreshold.value = threshold
        findSwipes()
    }

    fun onWindowSizeChange(size: Int) {
        _windowSize.value = size
        findSwipes()
    }

    private fun findSwipes() {
        audioData?.let {
            _swipes.value = AudioParser.findSwipes(it, _zcrThreshold.value, _windowSize.value)
        }
    }

    fun onSwipeSelected(swipe: Swipe) {
        _selectedSwipe.value = swipe
    }

    fun createTrimmedWavFile() {
        viewModelScope.launch {
            val swipe = _selectedSwipe.value
            val data = audioData
            if (swipe != null && data != null) {
                val trimmedData = data.sliceArray(swipe.start..swipe.end)
                try {
                    val file = AudioParser.createWavFile(trimmedData, 44100)
                    _trimmedFilePath.value = file.absolutePath
                } catch (e: Exception) {
                    _errorMessage.value = "Error creating trimmed WAV file: ${e.message}"
                }
            }
        }
    }
}
