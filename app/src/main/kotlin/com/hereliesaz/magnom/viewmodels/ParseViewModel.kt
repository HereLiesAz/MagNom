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
import com.hereliesaz.magnom.audio.AudioDecoder
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

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
    val trimmedFilePath: String? = null,
    val potentialTracks: List<String> = emptyList() // Added: List of decoded tracks
)

class ParseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ParseScreenState())
    val uiState: StateFlow<ParseScreenState> = _uiState
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun onZoom(zoom: Float) {
        _uiState.update { it.copy(zoom = it.zoom * zoom) }
    }

    fun setPan(pan: Float) {
        _uiState.update { it.copy(panOffset = pan) }
    }

    fun onFileSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFileUri = uri, errorMessage = null, audioData = shortArrayOf(), waveformData = null, swipes = emptyList(), potentialTracks = emptyList()) }
            try {
                val audioData = readWavFile(context, uri)
                val waveformData = generateWaveformData(audioData)
                val swipes = detectSwipes(audioData, _uiState.value.zcrThreshold, _uiState.value.windowSize)

                // Decode on Default dispatcher (CPU intensive)
                val decodedTracks = withContext(Dispatchers.Default) {
                     AudioDecoder.decode(audioData)
                }

                _uiState.update {
                    it.copy(
                        audioData = audioData,
                        waveformData = waveformData,
                        swipes = swipes,
                        potentialTracks = decodedTracks
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error parsing file: ${e.message}") }
            }
        }
    }

    fun getAvailableRecordingDevices(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        _uiState.update { it.copy(availableDevices = devices.toList()) }
    }

    fun onDeviceSelected(device: AudioDeviceInfo) {
        _uiState.update { it.copy(selectedDevice = device) }
    }

    fun startRecording(context: Context, device: AudioDeviceInfo?) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(errorMessage = "RECORD_AUDIO permission not granted.") }
            return
        }

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        device?.let { audioRecord?.preferredDevice = it }

        val outputFile = File(context.cacheDir, "recording.pcm")
        _uiState.update { it.copy(isRecording = true, savedFilePath = outputFile.absolutePath, audioData = shortArrayOf(), waveformData = emptyList(), potentialTracks = emptyList()) }

        audioRecord?.startRecording()

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            val audioBuffer = ShortArray(bufferSize / 2)
            val allAudioData = mutableListOf<Short>()
            val outputStream = FileOutputStream(outputFile)

            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                if (readSize > 0) {
                    allAudioData.addAll(audioBuffer.take(readSize))
                    val byteBuffer = ByteBuffer.allocate(readSize * 2).order(ByteOrder.LITTLE_ENDIAN)
                    byteBuffer.asShortBuffer().put(audioBuffer, 0, readSize)
                    outputStream.write(byteBuffer.array())

                    // Update waveform periodically
                    if (allAudioData.size % (sampleRate / 2) < bufferSize) { // Update ~twice a second
                        val currentData = allAudioData.toShortArray()
                        val waveform = generateWaveformData(currentData)
                        _uiState.update { it.copy(audioData = currentData, waveformData = waveform) }
                    }
                }
            }
            outputStream.close()
        }
    }

    fun stopRecording() {
        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
        }
        audioRecord?.release()
        audioRecord = null
        recordingJob?.cancel()
        _uiState.update { it.copy(isRecording = false) }

        // Final processing after recording stops
        viewModelScope.launch {
            val audioData = _uiState.value.audioData
            val swipes = detectSwipes(audioData, _uiState.value.zcrThreshold, _uiState.value.windowSize)

            // Decode recorded audio
            val decodedTracks = withContext(Dispatchers.Default) {
                 AudioDecoder.decode(audioData)
            }

            _uiState.update { it.copy(swipes = swipes, potentialTracks = decodedTracks) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecord?.release()
        recordingJob?.cancel()
    }

    fun onZcrThresholdChange(threshold: Double) {
        _uiState.update { it.copy(zcrThreshold = threshold) }
    }

    fun onWindowSizeChange(size: Int) {
        _uiState.update { it.copy(windowSize = size) }
    }

    fun onSwipeSelected(swipe: Swipe) {
        viewModelScope.launch {
             _uiState.update { it.copy(selectedSwipe = swipe) }
             // Optionally decode just this swipe
             val audioData = _uiState.value.audioData
             if (swipe.end <= audioData.size && swipe.start >= 0) {
                 val swipeData = audioData.sliceArray(swipe.start until swipe.end)
                 val decodedTracks = withContext(Dispatchers.Default) {
                     AudioDecoder.decode(swipeData)
                 }
                 _uiState.update { it.copy(potentialTracks = decodedTracks) }
             }
        }
    }

    fun createTrimmedWavFile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedSwipe = _uiState.value.selectedSwipe ?: return@launch
            val audioData = _uiState.value.audioData
            val trimmedData = audioData.sliceArray(selectedSwipe.start until selectedSwipe.end)
            val outputFile = File(context.cacheDir, "trimmed_swipe.wav")

            try {
                writeWavFile(outputFile, trimmedData)
                _uiState.update { it.copy(trimmedFilePath = outputFile.absolutePath) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error creating trimmed file: ${e.message}") }
            }
        }
    }

    private fun writeWavFile(file: File, data: ShortArray) {
        val byteBuffer = ByteBuffer.allocate(data.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asShortBuffer().put(data)
        val audioDataBytes = byteBuffer.array()

        val outputStream = FileOutputStream(file)
        writeWavHeader(outputStream, audioDataBytes.size)
        outputStream.write(audioDataBytes)
        outputStream.close()
    }

    private fun writeWavHeader(stream: FileOutputStream, dataSize: Int) {
        val sampleRate = 44100
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalDataLen = dataSize + 36
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte(); header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte(); header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Sub-chunk size (16 for PCM)
        header[20] = 1; header[21] = 0 // Audio format (1 for PCM)
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte(); header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte(); header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte(); header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte(); header[33] = 0 // Block align
        header[34] = bitsPerSample.toByte(); header[35] = 0 // Bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte(); header[41] = (dataSize shr 8 and 0xff).toByte()
        header[42] = (dataSize shr 16 and 0xff).toByte(); header[43] = (dataSize shr 24 and 0xff).toByte()

        stream.write(header)
    }

    private suspend fun readWavFile(context: Context, uri: Uri): ShortArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val headerBuffer = ByteArray(44)
            inputStream.read(headerBuffer)

            // A more robust WAV parsing logic
            if (headerBuffer[0] != 'R'.code.toByte() || headerBuffer[1] != 'I'.code.toByte() || headerBuffer[2] != 'F'.code.toByte() || headerBuffer[3] != 'F'.code.toByte()) {
                throw Exception("Not a valid WAV file (RIFF header not found)")
            }

            // Find the 'data' chunk
            var dataChunkSize = 0
            var offset = 12 // Skip RIFF header and WAVE identifier
            while (offset < headerBuffer.size - 8) {
                if (headerBuffer[offset] == 'd'.code.toByte() && headerBuffer[offset + 1] == 'a'.code.toByte() && headerBuffer[offset + 2] == 't'.code.toByte() && headerBuffer[offset + 3] == 'a'.code.toByte()) {
                    dataChunkSize = ByteBuffer.wrap(headerBuffer, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
                    break
                }
                val chunkSize = ByteBuffer.wrap(headerBuffer, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
                offset += 8 + chunkSize
            }

            if (dataChunkSize == 0) {
                 // fallback for non-standard wav files or if logic above fails to find chunk
                val bytes = inputStream.readBytes()
                val shorts = ShortArray(bytes.size / 2)
                ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
                return@use shorts
            }


            val bytes = ByteArray(dataChunkSize)
            inputStream.read(bytes)
            val shorts = ShortArray(bytes.size / 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            shorts
        } ?: throw Exception("Could not open file")
    }

    private fun generateWaveformData(audioData: ShortArray, targetPoints: Int = 1024): List<Float> {
        if (audioData.isEmpty()) return emptyList()

        val waveform = mutableListOf<Float>()
        val samplesPerPoint = audioData.size / targetPoints
        if (samplesPerPoint < 1) {
            // If data is smaller than target points, just convert all to float
            return audioData.map { it / 32768.0f }
        }

        for (i in 0 until targetPoints) {
            val start = i * samplesPerPoint
            val end = (start + samplesPerPoint).coerceAtMost(audioData.size)
            var max = 0.0f
            for (j in start until end) {
                val sample = audioData[j] / 32768.0f
                if (kotlin.math.abs(sample) > max) {
                    max = kotlin.math.abs(sample)
                }
            }
            waveform.add(max)
        }
        return waveform
    }

    private fun detectSwipes(audioData: ShortArray, threshold: Double, windowSize: Int): List<Swipe> {
        if (audioData.isEmpty() || windowSize <= 0) return emptyList()

        val swipes = mutableListOf<Swipe>()
        val windowEnergies = audioData.asSequence()
            .windowed(windowSize, windowSize, partialWindows = true)
            .map { window ->
                sqrt(window.map { it.toDouble() * it.toDouble() }.sum() / window.size)
            }
            .toList()

        val maxEnergy = windowEnergies.maxOrNull() ?: return emptyList()
        if (maxEnergy == 0.0) return emptyList()

        val normalizedEnergies = windowEnergies.map { it / maxEnergy }

        var inSwipe = false
        var swipeStartSample = 0

        normalizedEnergies.forEachIndexed { index, energy ->
            val isAboveThreshold = energy > threshold
            if (isAboveThreshold && !inSwipe) {
                inSwipe = true
                swipeStartSample = index * windowSize
            } else if (!isAboveThreshold && inSwipe) {
                inSwipe = false
                val swipeEndSample = (index * windowSize)
                swipes.add(Swipe(start = swipeStartSample, end = swipeEndSample))
            }
        }

        if (inSwipe) {
            swipes.add(Swipe(start = swipeStartSample, end = audioData.size))
        }

        return swipes
    }
}
