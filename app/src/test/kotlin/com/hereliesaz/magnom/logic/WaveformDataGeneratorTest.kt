package com.hereliesaz.magnom.logic

import org.junit.Assert.*
import org.junit.Test

class WaveformDataGeneratorTest {

    private val generator = WaveformDataGenerator(sampleRate = 44100, samplesPerBit = 20)

    @Test
    fun `test Track 2 generation and bitstream`() {
        val data = ";0?"
        val bitstream = generator.stringToBitstream(data, zeros = 0)

        // Correct values based on code verification:
        // ; (11) -> 1011 -> Parity 0 -> 1, 1, 0, 1, 0
        // 0 (0)  -> 0000 -> Parity 1 -> 0, 0, 0, 0, 1
        // ? (15) -> 1111 -> Parity 1 -> 1, 1, 1, 1, 1
        // LRC (4) -> 0100 -> Parity 0 -> 0, 0, 1, 0, 0

        val expected = listOf(
            1, 1, 0, 1, 0,
            0, 0, 0, 0, 1,
            1, 1, 1, 1, 1,
            0, 0, 1, 0, 0
        )

        if (expected != bitstream) {
            println("Expected: $expected")
            println("Actual:   $bitstream")
        }
        assertEquals(expected, bitstream)
    }

    @Test
    fun `test Track 1 generation`() {
        val data = "%E?"
        val bitstream = generator.stringToBitstream(data, zeros = 0)
        assertEquals(28, bitstream.size)
    }

    @Test
    fun `test waveform generation`() {
        val data = ";0?"
        val waveform = generator.generate(data, zeros = 0)
        assertEquals(400, waveform.size)
    }
}
