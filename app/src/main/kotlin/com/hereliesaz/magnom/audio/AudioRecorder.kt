package com.hereliesaz.magnom.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioRecord
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import java.io.File

/**
 * specialized class for handling audio recording from the device's microphone or input jack.
 *
 * It wraps the Android [AudioRecord] API to provide a simpler interface for starting
 * and stopping recording, and streaming the raw PCM data via a callback.
 *
 * @property context Application context for permission checks and system services.
 * @property selectedDevice Specific input device to use (e.g., headset mic), or null for default.
 * @property onAudioData Callback function invoked when a buffer of audio data is read.
 */
class AudioRecorder(
    private val context: Context,
    private val selectedDevice: AudioDeviceInfo?,
    private val onAudioData: (List<Short>) -> Unit
) {
    // Android AudioRecord instance
    private var audioRecord: AudioRecord? = null
    // Flag to control the recording loop
    private var isRecording = false
    // Calculate minimum buffer size required for the configuration
    private val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

    /**
     * Starts the audio recording process.
     *
     * Checks for permissions, initializes the AudioRecord object, and starts a background thread
     * to read data from the hardware buffer.
     */
    fun startRecording() {
        // Ensure we have permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Use MIC source (which can be routed from headset jack)
        val audioSource = MediaRecorder.AudioSource.MIC

        // Configure the recorder: 44.1kHz, Mono, 16-bit PCM
        audioRecord = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        // Explicitly route to selected device if provided (Android 23+)
        selectedDevice?.let {
            audioRecord?.preferredDevice = it
        }

        isRecording = true
        audioRecord?.startRecording()

        // Start reading loop in a separate thread
        Thread {
            val audioData = ShortArray(bufferSize)
            while (isRecording) {
                // Blocking read from audio hardware
                val read = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                if (read > 0) {
                    // Send data to callback (create sublist to avoid sending trailing zeros)
                    onAudioData(audioData.toList().subList(0, read))
                }
            }
        }.start()
    }

    /**
     * Stops the recording and releases resources.
     *
     * @param onRecordingStopped Callback invoked when recording has fully stopped.
     */
    fun stopRecording(onRecordingStopped: (String) -> Unit) {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        // The file path is handled by the ViewModel, so we can just pass an empty string here
        onRecordingStopped("")
    }

    companion object {
        /**
         * Helper to list all available audio input devices.
         */
        fun getAvailableRecordingDevices(context: Context): List<AudioDeviceInfo> {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Filter for INPUT devices (microphones, etc.)
            return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
        }
    }
}
