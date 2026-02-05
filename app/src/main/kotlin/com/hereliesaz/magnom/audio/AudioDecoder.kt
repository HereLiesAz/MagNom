package com.hereliesaz.magnom.audio

import kotlin.math.abs
import com.hereliesaz.magnom.logic.BinaryDecoder

/**
 * Decodes magnetic stripe audio data (F2F) into binary strings.
 *
 * This object implements the logic to convert analog audio waveforms (F2F encoded)
 * into digital bits by detecting flux transitions (peaks) and measuring the
 * intervals between them (Aiken Biphase).
 */
object AudioDecoder {

    /**
     * Decodes the audio data into potential track strings.
     *
     * Attempts to decode the signal in both forward and reverse directions to
     * account for the physical swipe direction.
     *
     * @param audioData The raw PCM audio samples.
     * @return A list of valid track strings found (e.g., Track 1 or Track 2 data).
     */
    fun decode(audioData: ShortArray): List<String> {
        // First, try to extract bits from the raw audio data
        var bits = extractBits(audioData)

        // If we found very few bits (likely just noise or square wave issues),
        // try calculating the derivative of the signal.
        // Differentiating turns square waves into spikes (peaks), which works better
        // with the peak detection algorithm.
        if (bits.size < 10) {
            val derivative = differentiate(audioData)
            bits = extractBits(derivative)
        }

        if (bits.isEmpty()) return emptyList()

        // Convert the list of bits (0s and 1s) into a string
        val bitString = bits.joinToString("")
        val results = mutableListOf<String>()

        // Try decoding the bit string as is (Forward swipe)
        try {
            results.add(BinaryDecoder.decode(bitString))
        } catch (e: Exception) {
            // Ignore decoding failures
        }

        // Try decoding the bit string in reverse (Reverse swipe)
        // Since F2F is self-clocking, we can just reverse the bit stream
        try {
            results.add(BinaryDecoder.decode(bitString.reversed()))
        } catch (e: Exception) {
            // Ignore decoding failures
        }

        // Return unique results only
        return results.distinct()
    }

    /**
     * Calculates the discrete derivative of the audio signal.
     *
     * Used to convert square-wave-like signals (from digital generation) into
     * peak-like signals (flux transitions) suitable for detection.
     *
     * @param audio Input audio array.
     * @return Differentiated audio array.
     */
    private fun differentiate(audio: ShortArray): ShortArray {
        if (audio.isEmpty()) return ShortArray(0)
        val diff = ShortArray(audio.size)

        var prev = audio[0].toInt()
        for (i in 1 until audio.size) {
            val curr = audio[i].toInt()
            // Calculate difference and scale/clamp to fit Short range
            val d = (curr - prev) / 2
            diff[i] = d.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            prev = curr
        }
        return diff
    }

    /**
     * Core F2F Decoding Logic.
     *
     * 1. Finds peaks (flux transitions) in the waveform.
     * 2. Calculates distances (intervals) between peaks.
     * 3. Classifies intervals as Short (1) or Long (0) based on adaptive thresholding.
     *
     * @param audio The audio waveform.
     * @return A list of bits (0 or 1).
     */
    private fun extractBits(audio: ShortArray): List<Int> {
        val peaks = findPeaks(audio)
        // Need at least a few peaks to form bits
        if (peaks.size < 10) return emptyList()

        val intervals = mutableListOf<Int>()
        for (i in 0 until peaks.size - 1) {
            // Interval is the distance in samples between consecutive peaks
            intervals.add(peaks[i+1] - peaks[i])
        }

        if (intervals.isEmpty()) return emptyList()

        // Synchronization: Look for a sequence of '0's (Long intervals) which typically
        // form the preamble (clocking zeros) of a magnetic stripe.
        var threshold = 0.0
        var foundSync = false
        var startIndex = 0

        // Limit search to first 50 intervals for efficiency
        val searchLimit = intervals.size.coerceAtMost(50)
        for (i in 0 until searchLimit - 5) {
            val window = intervals.subList(i, i + 5)
            val avg = window.average()
            // Calculate variance to check for consistency
            val variance = window.sumOf { (it - avg) * (it - avg) } / 5.0

            // If variance is low (intervals are consistent), we found the preamble
            if (variance < 0.04 * avg * avg) {
                // Set initial threshold. 0s are Long intervals (T). 1s are Short (T/2).
                // Threshold set at 0.75 * T determines the cutoff.
                threshold = avg * 0.75
                startIndex = i
                foundSync = true
                break
            }
        }

        // Fallback: If no sync pattern found, just guess based on the start
        if (!foundSync) {
             threshold = intervals.take(10).average() * 0.75
        }

        val bits = mutableListOf<Int>()
        var i = startIndex

        while (i < intervals.size) {
            val interval = intervals[i]

            // Compare interval to threshold
            if (interval < threshold) {
                // Short interval detected. In F2F, a '1' is encoded as two short intervals
                // within one bit period. So we expect another short interval immediately.
                if (i + 1 < intervals.size) {
                    val next = intervals[i+1]
                    // (Ideally we should check if 'next' is also short, but we assume validity for now)

                    bits.add(1)
                    i += 2 // Consumed two intervals

                    // Update adaptive threshold based on the full bit period (sum of two shorts)
                    threshold = (interval + next) * 0.75
                } else {
                    // Running out of data mid-bit
                    break
                }
            } else {
                // Long interval detected. Represents a '0'.
                bits.add(0)
                i += 1 // Consumed one interval

                // Update adaptive threshold based on this interval
                threshold = interval * 0.75
            }
        }

        return bits
    }

    /**
     * Finds local peaks (maxima and minima) in the audio signal.
     *
     * @param audio The audio waveform.
     * @return A list of indices where peaks occur.
     */
    private fun findPeaks(audio: ShortArray): List<Int> {
        val peaks = mutableListOf<Int>()

        // Establish a noise floor to ignore silence
        val maxAmp = audio.maxOfOrNull { abs(it.toInt()) } ?: 0
        val noiseFloor = maxAmp * 0.1

        var lookingForPositive = true

        // Determine initial phase: scan for first significant sample
        for (i in 0 until audio.size) {
            if (audio[i] > noiseFloor) {
                 lookingForPositive = true // Found positive value, looking for positive peak
                 break
            } else if (audio[i] < -noiseFloor) {
                 lookingForPositive = false // Found negative value, looking for negative peak
                 break
            }
        }

        for (i in 1 until audio.size - 1) {
            val prev = audio[i-1]
            val curr = audio[i]
            val next = audio[i+1]

            // Skip low-amplitude noise
            if (abs(curr.toInt()) < noiseFloor) continue

            if (lookingForPositive) {
                // Check for local maximum
                if (curr >= prev && curr >= next && curr > 0) {
                    peaks.add(i)
                    lookingForPositive = false // Switch to looking for negative peak
                }
            } else {
                // Check for local minimum
                if (curr <= prev && curr <= next && curr < 0) {
                    peaks.add(i)
                    lookingForPositive = true // Switch to looking for positive peak
                }
            }
        }
        return peaks
    }
}
