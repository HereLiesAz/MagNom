package com.hereliesaz.magnom.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecordingViewModel : ViewModel() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var outputFile: File? = null

    private val _audioData = MutableStateFlow<ShortArray>(shortArrayOf())
    val audioData: StateFlow<ShortArray> = _audioData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _savedFilePath = MutableStateFlow<String?>(null)
    val savedFilePath: StateFlow<String?> = _savedFilePath

    private val _availableDevices = MutableStateFlow<List<AudioDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<AudioDeviceInfo>> = _availableDevices

    private val _selectedDevice = MutableStateFlow<AudioDeviceInfo?>(null)
    val selectedDevice: StateFlow<AudioDeviceInfo?> = _selectedDevice

    fun getAvailableRecordingDevices(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        _availableDevices.value = devices.toList()
    }

    fun onDeviceSelected(device: AudioDeviceInfo) {
        _selectedDevice.value = device
    }

    fun startRecording(context: Context, device: AudioDeviceInfo?) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _errorMessage.value = "RECORD_AUDIO permission not granted"
            return
        }
        outputFile = File(context.cacheDir, "recording.pcm")
        val audioSource = device?.id ?: MediaRecorder.AudioSource.MIC
        audioRecord = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(1024)
            .build()

        if (device != null) {
            audioRecord?.preferredDevice = device
        }

        audioRecord?.startRecording()
        isRecording = true
        viewModelScope.launch {
            try {
                FileOutputStream(outputFile).use { fileOutputStream ->
                    while (isRecording) {
                        val buffer = ShortArray(1024)
                        val read = audioRecord?.read(buffer, 0, buffer.size)
                        if (read != null && read > 0) {
                            _audioData.value = buffer
                            fileOutputStream.write(buffer.toByteArray())
                        }
                    }
                }
                val wavFile = File(context.cacheDir, "recording.wav")
                addWavHeader(outputFile!!, wavFile)
                _savedFilePath.value = wavFile.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                _errorMessage.value = "An error occurred while saving the recording."
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun ShortArray.toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asShortBuffer().put(this)
        return byteBuffer.array()
    }

    private fun addWavHeader(pcmFile: File, wavFile: File) {
        val pcmInputStream = FileInputStream(pcmFile)
        val wavOutputStream = FileOutputStream(wavFile)
        val pcmData = pcmInputStream.readBytes()
        val sampleRate = 44100
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val dataSize = pcmData.size
        val fileSize = dataSize + 36

        wavOutputStream.write("RIFF".toByteArray())
        wavOutputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fileSize).array())
        wavOutputStream.write("WAVE".toByteArray())
        wavOutputStream.write("fmt ".toByteArray())
        wavOutputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
        wavOutputStream.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
        wavOutputStream.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(numChannels.toShort()).array())
        wavOutputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
        wavOutputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate).array())
        wavOutputStream.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(blockAlign.toShort()).array())
        wavOutputStream.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(bitsPerSample.toShort()).array())
        wavOutputStream.write("data".toByteArray())
        wavOutputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize).array())
        wavOutputStream.write(pcmData)

        pcmInputStream.close()
        wavOutputStream.close()
    }
}
