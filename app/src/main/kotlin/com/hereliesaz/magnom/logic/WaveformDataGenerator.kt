package com.hereliesaz.magnom.logic

/**
 * Generates raw audio waveform data from magnetic stripe track strings
 * based on the ISO/IEC 7811 F2F (Frequency/Double Frequency) encoding standard.
 *
 * Implements features from ViolentMag (MalfunctionMag.py):
 * - Support for Track 1, 2, and 3.
 * - Configurable samples per bit and leading zeros.
 * - Forward, Reverse, and Mimic (Back-and-Forth) swipe generation.
 */
class WaveformDataGenerator(
    private val sampleRate: Int = 44100,
    private val samplesPerBit: Int = 28 // Default ~1575 bps at 44.1kHz (44100/1575 = 28)
) {

    // Track 2/3 uses a 5-bit character set (4 data bits + 1 odd parity bit).
    // Track 1 uses a 7-bit character set (6 data bits + 1 odd parity bit).

    enum class TrackType(val bits: Int, val baseChar: Int, val maxVal: Int) {
        TRACK1(7, 32, 63), // Base ' ' (32)
        TRACK2(5, 48, 15), // Base '0' (48)
        TRACK3(5, 48, 15)  // Base '0' (48)
    }

    private fun getTrackType(trackData: String): TrackType {
        return if (trackData.startsWith("%")) {
            TrackType.TRACK1
        } else {
            TrackType.TRACK2 // Default to Track 2 for ';' start or others
        }
    }

    /**
     * Converts a full track string into its corresponding bitstream, including parity.
     */
    fun stringToBitstream(trackData: String, zeros: Int = 20): List<Int> {
        val trackType = getTrackType(trackData)
        val bitstream = mutableListOf<Int>()

        // Leading zeros
        repeat(zeros) { bitstream.add(0) }

        val lrc = IntArray(trackType.bits) // Rolling LRC

        for (char in trackData) {
            val raw = char.code - trackType.baseChar
            if (raw < 0 || raw > trackType.maxVal) {
                 // Fallback or ignore? ViolentMag throws error/exits. We will ignore or treat as 0?
                 // Ideally throw to let caller know input is bad.
                 throw IllegalArgumentException("Illegal character: $char for ${trackType.name}")
            }

            var parity = 1 // Odd parity start

            // Iterate data bits (bits - 1)
            for (i in 0 until trackType.bits - 1) {
                val bit = (raw shr i) and 1
                bitstream.add(bit)
                parity += bit
                lrc[i] = lrc[i] xor bit
            }

            // Parity bit
            val parityBit = parity % 2
            bitstream.add(parityBit)
        }

        // LRC Character
        var lrcParity = 1
        for (i in 0 until trackType.bits - 1) {
            bitstream.add(lrc[i])
            lrcParity += lrc[i]
        }
        // LRC Parity bit
        bitstream.add(lrcParity % 2)

        // Trailing zeros (same as leading for symmetry in simple generation)
        repeat(zeros) { bitstream.add(0) }

        return bitstream
    }

    /**
     * Generates a square wave PCM float array based on the F2F encoding of the track data.
     */
    fun generate(trackData: String, zeros: Int = 20): FloatArray {
        val bitstream = stringToBitstream(trackData, zeros)
        return bitsToPcm(bitstream)
    }

    /**
     * Generates a reverse swipe waveform.
     */
    fun generateReverse(trackData: String, zeros: Int = 20): FloatArray {
        val bitstream = stringToBitstream(trackData, zeros)
        // Reverse the bitstream effectively mimics a reverse swipe in magnetic domain?
        // ViolentMag says: data = data[::-1] (reverses the bit string)
        return bitsToPcm(bitstream.reversed())
    }

    /**
     * Generates a "mimic" swipe: Forward swipe, silence, then Reverse swipe.
     */
    fun mimicSwipe(trackData: String, zeros: Int = 20, silenceMs: Int = 500): FloatArray {
        val forward = generate(trackData, zeros)
        val reverse = generateReverse(trackData, zeros)

        val silenceSamples = (sampleRate * silenceMs) / 1000
        val silence = FloatArray(silenceSamples) { 0f }

        return forward + silence + reverse
    }

    private fun bitsToPcm(bitstream: List<Int>): FloatArray {
        val pcmData = FloatArray(bitstream.size * samplesPerBit)
        var currentLevel = 1.0f
        var writeHead = 0

        for (bit in bitstream) {
            val halfBitSamples = samplesPerBit / 2
            val fullBitSamples = samplesPerBit

            if (bit == 1) {
                currentLevel *= -1
            }

            for (i in 0 until halfBitSamples) {
                pcmData[writeHead++] = currentLevel
            }

            currentLevel *= -1

            for (i in 0 until (fullBitSamples - halfBitSamples)) {
                 pcmData[writeHead++] = currentLevel
            }
        }
        return pcmData
    }
}
