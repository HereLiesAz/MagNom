package com.hereliesaz.magnom.audio

import com.hereliesaz.magnom.data.Swipe
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility object for parsing WAV files and identifying magnetic swipes.
 *
 * This object provides functions to read standard WAV files into raw PCM arrays
 * and to analyze that data to find regions of high activity (swipes) using
 * energy or Zero Crossing Rate (ZCR) analysis.
 */
object AudioParser {

    /**
     * Reads a WAV file from an input stream and returns raw PCM data.
     *
     * It parses the RIFF header to determine the sample rate and extracts the raw audio samples.
     * Note: This implementation assumes a standard PCM WAV file (16-bit, Little Endian).
     *
     * @param inputStream Stream to read from.
     * @return A pair containing the audio data (ShortArray) and the sample rate (Int).
     */
    fun readWavFile(inputStream: InputStream): Pair<ShortArray, Int> {
        // Read the 44-byte WAV header
        val buffer = ByteArray(44)
        inputStream.read(buffer)

        // Parse sample rate from header (offset 24, 4 bytes)
        val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        val sampleRate = byteBuffer.getInt(24)

        // Read the rest of the data
        val audioData = mutableListOf<Short>()
        val audioBuffer = ByteArray(1024)
        var bytesRead: Int

        while (inputStream.read(audioBuffer).also { bytesRead = it } != -1) {
            // Convert bytes to shorts (little-endian)
            val shortBuffer = ByteBuffer.wrap(audioBuffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val shortArray = ShortArray(shortBuffer.remaining())
            shortBuffer.get(shortArray)
            audioData.addAll(shortArray.toList())
        }

        inputStream.close()
        return Pair(audioData.toShortArray(), sampleRate)
    }

    /**
     * Scans audio data for high-energy segments that indicate a card swipe.
     *
     * It uses a sliding window approach to calculate the Zero Crossing Rate (ZCR).
     * High ZCR typically indicates the presence of the high-frequency F2F signal against background noise.
     *
     * @param audioData Raw PCM audio.
     * @param zcrThreshold Threshold for ZCR detection (0.0 - 1.0).
     * @param windowSize Size of the analysis window in samples.
     * @return List of identified [Swipe] regions.
     */
    fun findSwipes(audioData: ShortArray, zcrThreshold: Double, windowSize: Int): List<Swipe> {
        val swipes = mutableListOf<Swipe>()
        var inSwipe = false
        var swipeStart = 0

        // Iterate through data in windows
        for (i in 0 until audioData.size - windowSize step windowSize) {
            val window = audioData.sliceArray(i until i + windowSize)
            val zcr = calculateZCR(window)

            // Trigger on ZCR rising above threshold
            if (zcr > zcrThreshold && !inSwipe) {
                inSwipe = true
                swipeStart = i
            } else if (zcr < zcrThreshold && inSwipe) {
                // End swipe when ZCR falls below threshold
                inSwipe = false
                swipes.add(Swipe(swipeStart, i + windowSize))
            }
        }

        return swipes
    }

    /**
     * Calculates the Zero Crossing Rate for a given window of audio.
     *
     * ZCR is the rate at which the signal changes sign.
     */
    private fun calculateZCR(audioData: ShortArray): Double {
        var crossings = 0
        for (i in 0 until audioData.size - 1) {
            // Check for sign change
            if ((audioData[i] > 0 && audioData[i + 1] <= 0) || (audioData[i] < 0 && audioData[i + 1] >= 0)) {
                crossings++
            }
        }
        return crossings.toDouble() / audioData.size
    }

    /**
     * Creates a new WAV file from a subset of audio data.
     *
     * Useful for saving a trimmed swipe.
     *
     * @param audioData The raw PCM data to write.
     * @param sampleRate The sample rate of the audio.
     * @return The created temporary File.
     */
    fun createWavFile(audioData: ShortArray, sampleRate: Int): File {
        val file = File.createTempFile("trimmed_audio", ".wav")
        val fos = FileOutputStream(file)

        // Convert Shorts back to Bytes (Little Endian)
        val byteBuffer = ByteBuffer.allocate(audioData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        val shortBuffer = byteBuffer.asShortBuffer()
        shortBuffer.put(audioData)

        // Write Header
        writeWavHeader(fos, audioData.size * 2, sampleRate)
        // Write Data
        fos.write(byteBuffer.array())
        fos.close()
        return file
    }

    /**
     * Writes the standard 44-byte WAV header to the stream.
     */
    private fun writeWavHeader(fos: FileOutputStream, totalAudioLen: Int, sampleRate: Int) {
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 16 / 8

        val header = ByteArray(44)
        // RIFF/WAVE header construction...
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * 16 / 8).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        fos.write(header, 0, 44)
    }
}
