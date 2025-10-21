package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.media.AudioDeviceInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioRecordingViewModel : ViewModel() {

    private val _audioData = MutableStateFlow<List<Short>>(emptyList())
    val audioData: StateFlow<List<Short>> = _audioData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _savedFilePath = MutableStateFlow<String?>(null)
    val savedFilePath: StateFlow<String?> = _savedFilePath.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<AudioDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<AudioDeviceInfo>> = _availableDevices.asStateFlow()

    private val _selectedDevice = MutableStateFlow<AudioDeviceInfo?>(null)
    val selectedDevice: StateFlow<AudioDeviceInfo?> = _selectedDevice.asStateFlow()

    private var audioRecorder: AudioRecorder? = null

    fun getAvailableRecordingDevices(context: Context) {
        _availableDevices.value = AudioRecorder.getAvailableRecordingDevices(context)
    }

    fun onDeviceSelected(device: AudioDeviceInfo) {
        _selectedDevice.value = device
    }

    fun startRecording(context: Context, device: AudioDeviceInfo?) {
        audioRecorder = AudioRecorder(context, device) {
            viewModelScope.launch {
                _audioData.value = it
            }
        }
        audioRecorder?.startRecording()
    }

    fun stopRecording() {
        audioRecorder?.stopRecording() {
            _savedFilePath.value = it
        }
    }
}
