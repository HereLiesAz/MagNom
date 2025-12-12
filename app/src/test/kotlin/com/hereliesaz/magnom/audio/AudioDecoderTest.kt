package com.hereliesaz.magnom.audio

import com.hereliesaz.magnom.logic.WaveformDataGenerator
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class AudioDecoderTest {

    private val generator = WaveformDataGenerator(sampleRate = 44100, samplesPerBit = 20)

    @Test
    fun `test Track 2 End-to-End Decoding`() {
        // Generate valid Track 2 data
        val trackData = ";123456789=99?"
        val waveform = generator.generate(trackData, zeros = 20)

        // Convert FloatArray to ShortArray
        val audioData = ShortArray(waveform.size)
        for (i in waveform.indices) {
            audioData[i] = (waveform[i] * 32767).roundToInt().toShort()
        }

        // Decode
        val decodedList = AudioDecoder.decode(audioData)

        assertTrue("Decoded list should contain original track data. Found: $decodedList", decodedList.contains(trackData))
    }

    @Test
    fun `test Track 1 End-to-End Decoding`() {
        // Generate valid Track 1 data
        val trackData = "%B123^NAME^?"
        val waveform = generator.generate(trackData, zeros = 20)

        val audioData = ShortArray(waveform.size)
        for (i in waveform.indices) {
            audioData[i] = (waveform[i] * 32767).roundToInt().toShort()
        }

        val decodedList = AudioDecoder.decode(audioData)

        assertTrue("Decoded list should contain original track data. Found: $decodedList", decodedList.contains(trackData))
    }

    @Test
    fun `test Reverse Decoding`() {
        val trackData = ";54321=11?"
        val waveform = generator.generateReverse(trackData, zeros = 20)

        val audioData = ShortArray(waveform.size)
        for (i in waveform.indices) {
            audioData[i] = (waveform[i] * 32767).roundToInt().toShort()
        }

        val decodedList = AudioDecoder.decode(audioData)

        assertTrue("Decoded list should contain original track data (from reverse swipe). Found: $decodedList", decodedList.contains(trackData))
    }
}
