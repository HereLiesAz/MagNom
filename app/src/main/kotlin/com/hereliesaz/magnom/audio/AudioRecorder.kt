package com.hereliesaz.magnom.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioFormat
import android.media.MediaRecorder
import java.io.File

class AudioRecorder(
    private val context: Context,
    private val selectedDevice: AudioDeviceInfo?,
    private val onAudioData: (List<Short>) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

    fun startRecording() {
        val audioSource = MediaRecorder.AudioSource.MIC

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

        selectedDevice?.let {
            audioRecord?.preferredDevice = it
        }

        isRecording = true
        audioRecord?.startRecording()

        Thread {
            val audioData = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                if (read > 0) {
                    onAudioData(audioData.toList().subList(0, read))
                }
            }
        }.start()
    }

    fun stopRecording(onRecordingStopped: (String) -> Unit) {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        // The file path is handled by the ViewModel, so we can just pass an empty string here
        onRecordingStopped("")
    }

    companion object {
        fun getAvailableRecordingDevices(context: Context): List<AudioDeviceInfo> {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
        }
    }
}
