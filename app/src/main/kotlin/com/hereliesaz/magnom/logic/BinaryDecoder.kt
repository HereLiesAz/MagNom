package com.hereliesaz.magnom.logic

object BinaryDecoder {

    /**
     * Decodes a raw binary string (composed of '0's and '1's) into text.
     * Supports Track 1 (7-bit) and Track 2/3 (5-bit) formats.
     * Searches for start/end sentinels and validates LRC.
     */
    fun decode(binaryData: String): String {
        // Try decoding as Track 1 first (7-bit)
        try {
            return decodeTrack(binaryData, 7, "1010001", "1111100") // % and ?
        } catch (e: Exception) {
            // Ignore and try Track 2
        }

        // Try decoding as Track 2 (5-bit)
        try {
            return decodeTrack(binaryData, 5, "11010", "11111") // ; and ?
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not decode data as Track 1 or Track 2.")
        }
    }

    private fun decodeTrack(
        data: String,
        bitsPerChar: Int,
        startSentinelPattern: String,
        endSentinelPattern: String
    ): String {
        // Find start sentinel
        val startIndex = data.indexOf(startSentinelPattern)
        if (startIndex < 0) {
            throw IllegalArgumentException("No start sentinel found")
        }

        // Find end sentinel
        // It must be aligned on character boundaries relative to start
        var endIndex = -1
        var searchIndex = startIndex + bitsPerChar
        while (searchIndex <= data.length - bitsPerChar) {
            val candidate = data.substring(searchIndex, searchIndex + bitsPerChar)
            if (candidate == endSentinelPattern) {
                endIndex = searchIndex
                break
            }
            searchIndex += bitsPerChar
        }

        if (endIndex == -1) {
             throw IllegalArgumentException("No end sentinel found")
        }

        val lrcStart = endIndex + bitsPerChar
        if (lrcStart + bitsPerChar > data.length) {
             throw IllegalArgumentException("Not enough data for LRC")
        }

        // Decode
        val sb = StringBuilder()
        val rollingLrc = IntArray(bitsPerChar)

        var currentIndex = startIndex
        // Include end sentinel in decoding loop to check its parity, but stop before LRC
        while (currentIndex <= endIndex) {
            val chunk = data.substring(currentIndex, currentIndex + bitsPerChar)

            var parity = 0
            var value = 0

            // Read data bits (LSB first usually in magstripe standard, but ViolentMag code reads linear)
            // ViolentMag:
            // asciichr += int(data[start_decode + x]) << x
            // So LSB is at start_decode (first bit of chunk)

            for (x in 0 until bitsPerChar - 1) {
                val bit = if (chunk[x] == '1') 1 else 0
                value += bit shl x
                parity += bit
                rollingLrc[x] = rollingLrc[x] xor bit
            }

            // Parity bit is the last one
            val parityBit = if (chunk[bitsPerChar - 1] == '1') 1 else 0
            parity += parityBit

            if (parity % 2 == 0) {
                throw IllegalArgumentException("Parity error at index $currentIndex")
            }

            // Map value to char
            val base = if (bitsPerChar == 7) 32 else 48
            sb.append((value + base).toChar())

            currentIndex += bitsPerChar
        }

        // Check LRC
        // ViolentMag:
        // actual_lrc = end_sentinel + 7
        // ...
        // rolling_lrc[6] = parity % 2

        val lrcChunk = data.substring(lrcStart, lrcStart + bitsPerChar)
        var lrcParity = 1 // Start with 1? ViolentMag: parity = 1.

        for (x in 0 until bitsPerChar - 1) {
            lrcParity += rollingLrc[x]
        }

        // Calculate expected parity bit for LRC
        val expectedLrcParityBit = lrcParity % 2

        // Verify LRC bits against rollingLrc
        for (x in 0 until bitsPerChar - 1) {
            val bit = if (lrcChunk[x] == '1') 1 else 0
            if (bit != rollingLrc[x]) {
                throw IllegalArgumentException("LRC mismatch at bit $x")
            }
        }

        // Verify LRC's own parity bit
        val lrcParityBit = if (lrcChunk[bitsPerChar - 1] == '1') 1 else 0
        if (lrcParityBit != expectedLrcParityBit) {
             throw IllegalArgumentException("LRC parity error")
        }

        return sb.toString()
    }
}
