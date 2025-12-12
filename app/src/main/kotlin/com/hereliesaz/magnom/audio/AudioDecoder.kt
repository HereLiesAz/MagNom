package com.hereliesaz.magnom.audio

import kotlin.math.abs
import com.hereliesaz.magnom.logic.BinaryDecoder

/**
 * Decodes magnetic stripe audio data (F2F) into binary strings.
 * Implements logic similar to RhombusLib.
 */
object AudioDecoder {

    /**
     * Decodes the audio data.
     * @param audioData The raw audio samples.
     * @return A list of potential track strings found.
     */
    fun decode(audioData: ShortArray): List<String> {
        val bits = extractBits(audioData)
        if (bits.isEmpty()) return emptyList()

        val bitString = bits.joinToString("")
        val results = mutableListOf<String>()

        // Try decoding forward
        try {
            results.add(BinaryDecoder.decode(bitString))
        } catch (e: Exception) {
            // Ignore failure
        }

        // Try decoding reverse
        try {
            results.add(BinaryDecoder.decode(bitString.reversed()))
        } catch (e: Exception) {
            // Ignore failure
        }

        return results.distinct()
    }

    /**
     * F2F Decoding:
     * 1. Find peaks (flux transitions).
     * 2. Calculate distances between peaks.
     * 3. Classify distances as Short (1) or Long (0).
     */
    private fun extractBits(audio: ShortArray): List<Int> {
        val peaks = findPeaks(audio)
        if (peaks.size < 10) return emptyList()

        val intervals = mutableListOf<Int>()
        for (i in 0 until peaks.size - 1) {
            intervals.add(peaks[i+1] - peaks[i])
        }

        // Calculate average interval to distinguish 0 and 1
        // '0' is one interval of length T.
        // '1' is two intervals of length T/2.

        // We will use a dynamic threshold.
        // If interval is ~ 2 * previous_short_interval, it's a 0.
        // If interval is ~ previous_short_interval, it's a half-bit of a 1.

        val bits = mutableListOf<Int>()
        var i = 0

        // Calibrate on the first few intervals (preamble usually 0s)
        // Preamble 0s mean we see regular long intervals.
        var threshold = intervals.take(10).average() * 0.75

        while (i < intervals.size) {
            val interval = intervals[i]

            // Check if it's a '1' (Short interval)
            // A '1' consists of two short intervals.
            if (interval < threshold) {
                // Ideally we should see another short interval next
                if (i + 1 < intervals.size) {
                    val next = intervals[i+1]
                    // Consume two short intervals for a '1'
                    bits.add(1)
                    i += 2
                    // Update threshold?
                    threshold = (interval + next) * 0.75
                } else {
                    break // End of data
                }
            } else {
                // It's a '0' (Long interval)
                bits.add(0)
                i += 1
                // Update threshold?
                threshold = interval * 0.75
            }
        }

        return bits
    }

    private fun findPeaks(audio: ShortArray): List<Int> {
        val peaks = mutableListOf<Int>()
        // Simple peak detection: Look for local maxima/minima or zero crossings?
        // F2F transitions are peaks in flux, which result in peaks in voltage (audio).
        // We look for significant peaks.

        // First, normalize or establish a noise floor
        val maxAmp = audio.maxOfOrNull { abs(it.toInt()) } ?: 0
        val noiseFloor = maxAmp * 0.1

        var lastPeakIndex = 0
        var lookingForPositive = true

        // Simple state machine to find alternating peaks
        for (i in 1 until audio.size - 1) {
            val prev = audio[i-1]
            val curr = audio[i]
            val next = audio[i+1]

            if (abs(curr.toInt()) < noiseFloor) continue

            if (lookingForPositive) {
                if (curr > prev && curr > next && curr > 0) {
                    peaks.add(i)
                    lastPeakIndex = i
                    lookingForPositive = false
                }
            } else {
                if (curr < prev && curr < next && curr < 0) {
                    peaks.add(i)
                    lastPeakIndex = i
                    lookingForPositive = true
                }
            }
        }
        return peaks
    }
}
