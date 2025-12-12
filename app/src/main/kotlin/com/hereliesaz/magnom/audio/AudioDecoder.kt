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
        // Try raw audio first
        var bits = extractBits(audioData)

        // If minimal bits found, try differentiating (for square waves)
        if (bits.size < 10) {
            val derivative = differentiate(audioData)
            bits = extractBits(derivative)
        }

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

    private fun differentiate(audio: ShortArray): ShortArray {
        if (audio.isEmpty()) return ShortArray(0)
        val diff = ShortArray(audio.size)
        // Simple difference: y[n] = x[n] - x[n-1]
        // Note: Result might exceed Short range if step is large (e.g. -32768 to 32767).
        // Square wave jump is ~65000.
        // We saturate or just wrap? Peak detection only needs relative magnitude.
        // Wrapping might invert peak direction.
        // Better to cast to Int, diff, then clamp or scale?
        // Let's scale by 0.5 to fit Short.

        var prev = audio[0].toInt()
        for (i in 1 until audio.size) {
            val curr = audio[i].toInt()
            val d = (curr - prev) / 2
            diff[i] = d.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            prev = curr
        }
        return diff
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

        if (intervals.isEmpty()) return emptyList()

        // Calculate average interval to distinguish 0 and 1
        // Preamble usually consists of '0's (Long intervals).
        // We look for a sequence of intervals with low variance.

        var threshold = 0.0
        var foundSync = false
        var startIndex = 0

        // Try to find sync in first 50 intervals
        val searchLimit = intervals.size.coerceAtMost(50)
        for (i in 0 until searchLimit - 5) {
            val window = intervals.subList(i, i + 5)
            val avg = window.average()
            val variance = window.sumOf { (it - avg) * (it - avg) } / 5.0

            // If variance is low relative to average (e.g. < 10% deviation)
            // 10% of 20 samples = 2. Variance < 4.
            // Let's say std dev < 0.2 * avg. Variance < 0.04 * avg^2.
            if (variance < 0.04 * avg * avg) {
                threshold = avg * 0.75
                startIndex = i
                foundSync = true
                break
            }
        }

        // Fallback if no sync found (e.g. very clean generated data without jitter but maybe short?)
        if (!foundSync) {
             threshold = intervals.take(10).average() * 0.75
        }

        val bits = mutableListOf<Int>()
        var i = startIndex

        // Skip the preamble 0s? No, decode them.

        while (i < intervals.size) {
            val interval = intervals[i]

            // Check if it's a '1' (Short interval)
            if (interval < threshold) {
                // Must have a second short interval
                if (i + 1 < intervals.size) {
                    val next = intervals[i+1]
                    // Ideally check if 'next' is also short ( < threshold)
                    // If next is Long, we have a sync error (Short followed by Long).
                    // But adaptive logic might handle it.

                    bits.add(1)
                    i += 2
                    // Update threshold: (interval + next) is roughly one bit duration (T)
                    threshold = (interval + next) * 0.75
                } else {
                    break
                }
            } else {
                // It's a '0' (Long interval)
                bits.add(0)
                i += 1
                // Update threshold: interval is roughly T
                threshold = interval * 0.75
            }
        }

        return bits
    }

    private fun findPeaks(audio: ShortArray): List<Int> {
        val peaks = mutableListOf<Int>()

        // First, normalize or establish a noise floor
        val maxAmp = audio.maxOfOrNull { abs(it.toInt()) } ?: 0
        val noiseFloor = maxAmp * 0.1

        var lastPeakIndex = 0
        var lookingForPositive = true

        // Initial search direction:
        // If we start negative, look for positive peak first.
        // If we start positive, look for negative peak?
        // Standard F2F has alternating flux.

        // Scan for first significant sample to determine phase
        for (i in 0 until audio.size) {
            if (audio[i] > noiseFloor) {
                 lookingForPositive = true // Found positive, so next peak is positive max?
                 // Wait, if we are climbing, we are looking for the top.
                 break
            } else if (audio[i] < -noiseFloor) {
                 lookingForPositive = false
                 break
            }
        }

        for (i in 1 until audio.size - 1) {
            val prev = audio[i-1]
            val curr = audio[i]
            val next = audio[i+1]

            if (abs(curr.toInt()) < noiseFloor) continue

            if (lookingForPositive) {
                // Local max
                if (curr >= prev && curr >= next && curr > 0) {
                     // Check if it's really a peak or just a plateau?
                     // Simple check: > prev and >= next

                     // De-bounce: ensure we moved far enough from last peak?
                     // F2F peaks are separated.

                    peaks.add(i)
                    lastPeakIndex = i
                    lookingForPositive = false
                }
            } else {
                // Local min
                if (curr <= prev && curr <= next && curr < 0) {
                    peaks.add(i)
                    lastPeakIndex = i
                    lookingForPositive = true
                }
            }
        }
        return peaks
    }
}
