package com.hereliesaz.magnom.core

/**
 * Minimal, dependency-free reader/writer for 16-bit PCM mono WAV data.
 *
 * Kept in commonMain so both platforms share exactly one WAV implementation; platform
 * code only supplies the bytes (a file on desktop, a content stream on Android).
 */
object Wav {

    /** Decode a little-endian 16-bit PCM mono WAV byte array into float samples in [-1, 1]. */
    fun decode(bytes: ByteArray): FloatArray {
        require(bytes.size > 44) { "WAV too short" }
        require(bytes[0].toInt() == 'R'.code && bytes[1].toInt() == 'I'.code &&
            bytes[2].toInt() == 'F'.code && bytes[3].toInt() == 'F'.code) { "Not a RIFF file" }

        var pos = 12 // skip RIFF header + WAVE
        var dataOffset = -1
        var dataLength = 0
        while (pos + 8 <= bytes.size) {
            val id = bytes.decodeToString(pos, pos + 4)
            val size = le32(bytes, pos + 4)
            if (id == "data") { dataOffset = pos + 8; dataLength = size; break }
            pos += 8 + size + (size and 1)
        }
        require(dataOffset >= 0) { "No data chunk" }

        val end = minOf(dataOffset + dataLength, bytes.size)
        val count = (end - dataOffset) / 2
        val out = FloatArray(count)
        var b = dataOffset
        for (i in 0 until count) {
            val lo = bytes[b].toInt() and 0xFF
            val hi = bytes[b + 1].toInt()
            out[i] = ((hi shl 8) or lo).toShort() / 32768f
            b += 2
        }
        return out
    }

    /** Encode float samples in [-1, 1] into a little-endian 16-bit PCM mono WAV byte array. */
    fun encode(samples: FloatArray, sampleRate: Int = F2f.DEFAULT_SAMPLE_RATE): ByteArray {
        val dataSize = samples.size * 2
        val out = ByteArray(44 + dataSize)
        fun putStr(off: Int, s: String) { for (i in s.indices) out[off + i] = s[i].code.toByte() }
        fun put32(off: Int, v: Int) {
            out[off] = (v and 0xFF).toByte(); out[off + 1] = ((v shr 8) and 0xFF).toByte()
            out[off + 2] = ((v shr 16) and 0xFF).toByte(); out[off + 3] = ((v shr 24) and 0xFF).toByte()
        }
        fun put16(off: Int, v: Int) { out[off] = (v and 0xFF).toByte(); out[off + 1] = ((v shr 8) and 0xFF).toByte() }

        putStr(0, "RIFF"); put32(4, 36 + dataSize); putStr(8, "WAVE")
        putStr(12, "fmt "); put32(16, 16); put16(20, 1); put16(22, 1)
        put32(24, sampleRate); put32(28, sampleRate * 2); put16(32, 2); put16(34, 16)
        putStr(36, "data"); put32(40, dataSize)

        var b = 44
        for (s in samples) {
            val clamped = if (s > 1f) 1f else if (s < -1f) -1f else s
            val v = (clamped * 32767f).toInt()
            put16(b, v); b += 2
        }
        return out
    }

    private fun le32(b: ByteArray, off: Int): Int =
        (b[off].toInt() and 0xFF) or ((b[off + 1].toInt() and 0xFF) shl 8) or
            ((b[off + 2].toInt() and 0xFF) shl 16) or ((b[off + 3].toInt() and 0xFF) shl 24)
}
