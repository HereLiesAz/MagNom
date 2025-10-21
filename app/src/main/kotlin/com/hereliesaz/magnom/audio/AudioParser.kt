package com.hereliesaz.magnom.audio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Swipe(val start: Int, val end: Int)

object AudioParser {

    fun readWavFile(file: File): Pair<ShortArray, Int> {
        val fis = FileInputStream(file)
        val buffer = ByteArray(44)
        fis.read(buffer)

        val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        val sampleRate = byteBuffer.getInt(24)

        val audioData = mutableListOf<Short>()
        val audioBuffer = ByteArray(1024)
        var bytesRead: Int

        while (fis.read(audioBuffer).also { bytesRead = it } != -1) {
            val shortBuffer = ByteBuffer.wrap(audioBuffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val shortArray = ShortArray(shortBuffer.remaining())
            shortBuffer.get(shortArray)
            audioData.addAll(shortArray.toList())
        }

        fis.close()
        return Pair(audioData.toShortArray(), sampleRate)
    }

    fun findSwipes(audioData: ShortArray, zcrThreshold: Double, windowSize: Int): List<Swipe> {
        val swipes = mutableListOf<Swipe>()
        var inSwipe = false
        var swipeStart = 0

        for (i in 0 until audioData.size - windowSize step windowSize) {
            val window = audioData.sliceArray(i until i + windowSize)
            val zcr = calculateZCR(window)

            if (zcr > zcrThreshold && !inSwipe) {
                inSwipe = true
                swipeStart = i
            } else if (zcr < zcrThreshold && inSwipe) {
                inSwipe = false
                swipes.add(Swipe(swipeStart, i + windowSize))
            }
        }

        return swipes
    }

    private fun calculateZCR(audioData: ShortArray): Double {
        var crossings = 0
        for (i in 0 until audioData.size - 1) {
            if ((audioData[i] > 0 && audioData[i + 1] <= 0) || (audioData[i] < 0 && audioData[i + 1] >= 0)) {
                crossings++
            }
        }
        return crossings.toDouble() / audioData.size
    }

    fun createWavFile(audioData: ShortArray, sampleRate: Int): File {
        val file = File.createTempFile("trimmed_audio", ".wav")
        val fos = FileOutputStream(file)
        val byteBuffer = ByteBuffer.allocate(audioData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        val shortBuffer = byteBuffer.asShortBuffer()
        shortBuffer.put(audioData)

        writeWavHeader(fos, audioData.size * 2, sampleRate)
        fos.write(byteBuffer.array())
        fos.close()
        return file
    }

    private fun writeWavHeader(fos: FileOutputStream, totalAudioLen: Int, sampleRate: Int) {
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 16 / 8

        val header = ByteArray(44)
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
