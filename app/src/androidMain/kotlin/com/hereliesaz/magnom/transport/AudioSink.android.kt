package com.hereliesaz.magnom.transport

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Plays F2F PCM through an AudioTrack (audio-jack / coil output). */
actual class AudioSink actual constructor() {
    private var track: AudioTrack? = null

    actual suspend fun play(pcm: FloatArray, sampleRate: Int) = withContext(Dispatchers.IO) {
        val shorts = ShortArray(pcm.size) { i ->
            val v = pcm[i]
            (when { v > 1f -> 1f; v < -1f -> -1f; else -> v } * Short.MAX_VALUE).toInt().toShort()
        }
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(shorts.size * 2)
        val at = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = at
        at.play()
        at.setVolume(AudioTrack.getMaxVolume())
        var off = 0
        while (off < shorts.size) {
            val written = at.write(shorts, off, shorts.size - off)
            if (written <= 0) break
            off += written
        }
        at.stop()
        at.release()
        track = null
    }

    actual fun stop() {
        track?.let { runCatching { it.pause(); it.flush(); it.release() } }
        track = null
    }
}
