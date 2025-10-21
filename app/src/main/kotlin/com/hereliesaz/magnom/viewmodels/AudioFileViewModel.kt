package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioParser
import com.hereliesaz.magnom.audio.Result
import com.hereliesaz.magnom.audio.Swipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioFileViewModel : ViewModel() {

    private var context: Context? = null
    private var audioParser: AudioParser? = null

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri

    private val _swipes = MutableStateFlow<List<Swipe>>(emptyList())
    val swipes: StateFlow<List<Swipe>> = _swipes

    private val _selectedSwipe = MutableStateFlow<Swipe?>(null)
    val selectedSwipe: StateFlow<Swipe?> = _selectedSwipe

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _zcrThreshold = MutableStateFlow(0.1)
    val zcrThreshold: StateFlow<Double> = _zcrThreshold

    private val _windowSize = MutableStateFlow(1024)
    val windowSize: StateFlow<Int> = _windowSize

    private val _trimmedFilePath = MutableStateFlow<String?>(null)
    val trimmedFilePath: StateFlow<String?> = _trimmedFilePath

    fun onZcrThresholdChange(threshold: Double) {
        _zcrThreshold.value = threshold
        reparse()
    }

    fun onWindowSizeChange(size: Int) {
        _windowSize.value = size
        reparse()
    }

    fun onFileSelected(context: Context, uri: Uri) {
        this.context = context
        _selectedFileUri.value = uri
        audioParser = AudioParser(context, uri, zcrThreshold.value, windowSize.value)
        reparse()
    }

    fun onSwipeSelected(swipe: Swipe) {
        _selectedSwipe.value = swipe
    }

    fun createTrimmedWavFile() {
        val currentSwipe = selectedSwipe.value ?: return
        val currentContext = context ?: return
        viewModelScope.launch {
            val outputFile = File(currentContext.cacheDir, "trimmed_swipe.wav")
            when (val result = audioParser?.createTrimmedWavFile(currentSwipe, outputFile)) {
                is Result.Success -> {
                    _trimmedFilePath.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _trimmedFilePath.value = null
                    _errorMessage.value = result.message
                }
                else -> {
                    _trimmedFilePath.value = null
                    _errorMessage.value = "An unknown error occurred"
                }
            }
        }
    }

    private fun reparse() {
        viewModelScope.launch {
            when (val result = audioParser?.parse()) {
                is Result.Success -> {
                    _swipes.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _swipes.value = emptyList()
                    _errorMessage.value = result.message
                }
                else -> {
                    _swipes.value = emptyList()
                    _errorMessage.value = "An unknown error occurred"
                }
            }
        }
    }
}
