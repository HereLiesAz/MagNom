package com.hereliesaz.magnom.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioParser(
    private val context: Context,
    private val uri: Uri,
    private val zcrThreshold: Double,
    private val windowSize: Int
) {

    fun parse(): Result<List<Swipe>> {
        return when (val result = decodeAudio()) {
            is Result.Success -> {
                val swipes = detectSwipes(result.data)
                if (swipes.isEmpty()) {
                    Result.Error("No swipes detected in the audio file.")
                } else {
                    Result.Success(swipes)
                }
            }
            is Result.Error -> result
        }
    }

    private fun decodeAudio(): Result<ShortArray> {
        try {
            val extractor = MediaExtractor()
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
            if (fileDescriptor == null) {
                return Result.Error("Could not open the audio file.")
            }
            extractor.setDataSource(fileDescriptor)

            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex == -1) {
                return Result.Error("Could not find an audio track in the file.")
            }
            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime == null) {
                return Result.Error("Could not determine the MIME type of the audio track.")
            }

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val outputStream = ByteArrayOutputStream()
            val bufferInfo = MediaCodec.BufferInfo()

            var isEOS = false
            while (!isEOS) {
                val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                    val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEOS = true
                    } else {
                        decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                var outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                    val outData = ByteArray(bufferInfo.size)
                    outputBuffer?.get(outData)
                    outputStream.write(outData)
                    decoder.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                }
            }

            decoder.stop()
            decoder.release()
            extractor.release()

            val decodedBytes = outputStream.toByteArray()
            val shorts = ShortArray(decodedBytes.size / 2)
            ByteBuffer.wrap(decodedBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            return Result.Success(shorts)
        } catch (e: IOException) {
            e.printStackTrace()
            return Result.Error("An error occurred while reading the audio file.")
        } catch (e: MediaCodec.CryptoException) {
            e.printStackTrace()
            return Result.Error("An error occurred while decoding the audio file.")
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                return i
            }
        }
        return -1
    }

    private fun detectSwipes(pcmData: ShortArray): List<Swipe> {
        val swipes = mutableListOf<Swipe>()
        val zcr = mutableListOf<Double>()

        for (i in 0 until pcmData.size - windowSize step windowSize) {
            var crossings = 0
            for (j in i until i + windowSize - 1) {
                if ((pcmData[j] > 0 && pcmData[j + 1] <= 0) || (pcmData[j] < 0 && pcmData[j + 1] >= 0)) {
                    crossings++
                }
            }
            val zcrValue = crossings.toDouble() / windowSize
            zcr.add(zcrValue)
            Log.d("AudioParser", "ZCR value: $zcrValue")
        }

        var inSwipe = false
        var startTime = 0L

        for ((index, value) in zcr.withIndex()) {
            if (value > zcrThreshold && !inSwipe) {
                inSwipe = true
                startTime = (index * windowSize).toLong()
            } else if (value < zcrThreshold && inSwipe) {
                inSwipe = false
                swipes.add(Swipe(startTime, (index * windowSize).toLong()))
            }
        }

        return swipes
    }
}
