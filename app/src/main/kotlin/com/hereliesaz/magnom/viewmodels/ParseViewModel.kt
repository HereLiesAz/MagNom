package com.hereliesaz.magnom.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioParser
import com.hereliesaz.magnom.data.Swipe
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.logic.AudioPlayer
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.logic.WaveformDataGenerator
import com.hereliesaz.magnom.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ParseState(
    // AudioFileViewModel
    val selectedFileUri: Uri? = null,
    val swipes: List<Swipe> = emptyList(),
    val selectedSwipe: Swipe? = null,
    val errorMessage: String? = null,
    val zcrThreshold: Double = 0.1,
    val windowSize: Int = 1024,
    val trimmedFilePath: String? = null,

    // AudioRecordingViewModel
    val audioData: ShortArray = shortArrayOf(),
    val savedFilePath: String? = null,
    val availableDevices: List<AudioDeviceInfo> = emptyList(),
    val selectedDevice: AudioDeviceInfo? = null,
    val isRecording: Boolean = false,

    // WaveformViewModel
    val waveformData: FloatArray? = null,
    val zoom: Float = 1f,
    val panOffset: Float = 0f,
    val trackData: String? = null,
    val isPlaying: Boolean = false
)

class ParseViewModel(
    private val cardRepository: CardRepository? = null,
    private val cardId: String? = null
) : ViewModel() {

    private var context: Context? = null
    private var audioRecord: AudioRecord? = null
    private var outputFile: File? = null

    private val waveformDataGenerator = WaveformDataGenerator()
    private val trackDataGenerator = TrackDataGenerator()
    private val audioPlayer = AudioPlayer()

    private val _uiState = MutableStateFlow(ParseState())
    val uiState: StateFlow<ParseState> = _uiState.asStateFlow()

    init {
        cardId?.let { loadCardData(it) }
    }

    // AudioFileViewModel logic
    fun onZcrThresholdChange(threshold: Double) {
        _uiState.value = _uiState.value.copy(zcrThreshold = threshold)
        reparse()
    }

    fun onWindowSizeChange(size: Int) {
        _uiState.value = _uiState.value.copy(windowSize = size)
        reparse()
    }

    fun onFileSelected(context: Context, uri: Uri) {
        this.context = context
        _uiState.value = _uiState.value.copy(selectedFileUri = uri)
        reparse()
    }

    fun onSwipeSelected(swipe: Swipe) {
        _uiState.value = _uiState.value.copy(selectedSwipe = swipe)
    }

    fun createTrimmedWavFile() {
        val currentSwipe = uiState.value.selectedSwipe ?: return
        val currentUri = uiState.value.selectedFileUri ?: return
        val currentContext = context ?: return

        viewModelScope.launch {
            try {
                currentContext.contentResolver.openInputStream(currentUri)?.use { inputStream ->
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

    private fun reparse() {
        val currentUri = uiState.value.selectedFileUri ?: return
        val currentContext = context ?: return
        viewModelScope.launch {
            try {
                currentContext.contentResolver.openInputStream(currentUri)?.use { inputStream ->
                    val audioData = AudioParser.readWavFile(inputStream)
                    val swipes = AudioParser.findSwipes(audioData.first, _uiState.value.zcrThreshold, _uiState.value.windowSize)
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

    // AudioRecordingViewModel logic
    fun getAvailableRecordingDevices(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        _uiState.value = _uiState.value.copy(availableDevices = devices.toList())
    }

    fun onDeviceSelected(device: AudioDeviceInfo) {
        _uiState.value = _uiState.value.copy(selectedDevice = device)
    }

    fun startRecording(context: Context, device: AudioDeviceInfo?) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.value = _uiState.value.copy(errorMessage = "RECORD_AUDIO permission not granted")
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
        _uiState.value = _uiState.value.copy(isRecording = true)
        viewModelScope.launch {
            try {
                FileOutputStream(outputFile).use { fileOutputStream ->
                    while (uiState.value.isRecording) {
                        val buffer = ShortArray(1024)
                        val read = audioRecord?.read(buffer, 0, buffer.size)
                        if (read != null && read > 0) {
                            _uiState.value = _uiState.value.copy(audioData = buffer)
                            fileOutputStream.write(buffer.toByteArray())
                        }
                    }
                }
                val wavFile = File(context.cacheDir, "recording.wav")
                addWavHeader(outputFile!!, wavFile)
                _uiState.value = _uiState.value.copy(savedFilePath = wavFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(errorMessage = "An error occurred while saving the recording.")
            }
        }
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
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

    // WaveformViewModel logic
    private fun loadCardData(cardId: String) {
        viewModelScope.launch {
            val card = cardRepository?.getCardProfile(cardId)
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
        val newZoom = (uiState.value.zoom * zoomFactor).coerceIn(0.1f, 10f)
        _uiState.value = _uiState.value.copy(zoom = newZoom)
    }

    fun setPan(pan: Float) {
        _uiState.value = _uiState.value.copy(panOffset = pan)
    }

    fun togglePlayback() {
        if (uiState.value.isPlaying) {
            audioPlayer.stop()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            uiState.value.waveformData?.let {
                audioPlayer.play(it)
                _uiState.value = _uiState.value.copy(isPlaying = true)
            }
        }
    }
}
