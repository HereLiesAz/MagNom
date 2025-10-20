package com.hereliesaz.magnom.logic

import kotlin.math.sin

/**
 * Generates raw audio waveform data from magnetic stripe track strings
 * based on the ISO/IEC 7811 F2F (Frequency/Double Frequency) encoding standard.
 */
class WaveformDataGenerator {

    private val sampleRate = 44100
    private val bitsPerSecond = 1575
    private val samplesPerBit = sampleRate / bitsPerSecond

    // Track 2 uses a 5-bit character set (4 data bits + 1 odd parity bit).
    private val track2CharMap = mapOf(
        '0' to "0000", '1' to "0001", '2' to "0010", '3' to "0011",
        '4' to "0100", '5' to "0101", '6' to "0110", '7' to "0111",
        '8' to "1000", '9' to "1001", ';' to "1011", '=' to "1101",
        '?' to "1111"
    )

    /**
     * Calculates and prepends an odd parity bit to a binary string.
     */
    private fun withOddParity(dataBits: String): String {
        val onesCount = dataBits.count { it == '1' }
        val parityBit = if (onesCount % 2 == 0) "1" else "0"
        return parityBit + dataBits
    }

    /**
     * Converts a full track string into its corresponding bitstream, including parity.
     */
    private fun stringToBitstream(trackData: String): List<Int> {
        return trackData.flatMap { char ->
            val dataBits = track2CharMap[char] ?: throw IllegalArgumentException("Invalid character in Track 2 data: $char")
            val bitsWithParity = withOddParity(dataBits)
            bitsWithParity.map { it.toString().toInt() }
        }
    }

    /**
     * Generates a square wave PCM float array based on the F2F encoding of the track data.
     *
     * @param trackData The complete Track 2 string (including sentinels and LRC).
     * @return A FloatArray of PCM data ranging from -1.0 to 1.0.
     */
    fun generate(trackData: String): FloatArray {
        val bitstream = stringToBitstream(trackData)
        val pcmData = FloatArray(bitstream.size * samplesPerBit)
        var currentLevel = 1.0f
        var writeHead = 0

        // F2F encoding:
        // '0' has a flux transition only in the middle of the bit cell.
        // '1' has a flux transition at the beginning and in the middle.
        for (bit in bitstream) {
            val halfBitSamples = samplesPerBit / 2
            val fullBitSamples = samplesPerBit

            if (bit == 1) {
                // Transition at the start of the bit cell for a '1'
                currentLevel *= -1
            }

            // First half of the bit cell
            for (i in 0 until halfBitSamples) {
                pcmData[writeHead++] = currentLevel
            }

            // Transition in the middle of the cell for both '0' and '1'
            currentLevel *= -1

            // Second half of the bit cell
            for (i in 0 until (fullBitSamples - halfBitSamples)) {
                 pcmData[writeHead++] = currentLevel
            }
        }
        return pcmData
    }
}
