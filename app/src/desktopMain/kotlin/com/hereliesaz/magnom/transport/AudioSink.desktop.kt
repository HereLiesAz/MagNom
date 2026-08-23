package com.hereliesaz.magnom.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

/** Plays F2F PCM through a javax.sound SourceDataLine (audio-jack / coil output on desktop). */
actual class AudioSink actual constructor() {
    @Volatile private var line: SourceDataLine? = null

    actual suspend fun play(pcm: FloatArray, sampleRate: Int) = withContext(Dispatchers.IO) {
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false) // signed, little-endian
        val bytes = ByteArray(pcm.size * 2)
        for (i in pcm.indices) {
            val v = (when { pcm[i] > 1f -> 1f; pcm[i] < -1f -> -1f; else -> pcm[i] } * Short.MAX_VALUE).toInt()
            bytes[i * 2] = (v and 0xFF).toByte()
            bytes[i * 2 + 1] = ((v shr 8) and 0xFF).toByte()
        }
        val l = AudioSystem.getSourceDataLine(format)
        line = l
        l.open(format)
        l.start()
        l.write(bytes, 0, bytes.size)
        l.drain()
        l.stop()
        l.close()
        line = null
    }

    actual fun stop() {
        line?.let { runCatching { it.stop(); it.close() } }
        line = null
    }
}
