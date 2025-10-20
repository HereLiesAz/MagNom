package com.hereliesaz.magnom.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioParser(private val context: Context, private val uri: Uri) {

    private val THRESHOLD = 0.1 // This is a placeholder value and will need to be tuned

    fun parse(): List<Swipe> {
        val pcmData = decodeAudio() ?: return emptyList()
        return detectSwipes(pcmData)
    }

    private fun decodeAudio(): ShortArray? {
        val extractor = MediaExtractor()
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor ?: return null
        extractor.setDataSource(fileDescriptor)

        val audioTrackIndex = findAudioTrack(extractor)
        if (audioTrackIndex == -1) {
            return null
        }
        extractor.selectTrack(audioTrackIndex)
        val format = extractor.getTrackFormat(audioTrackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return null

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
        return shorts
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
        var inSwipe = false
        var startTime = 0L

        for ((index, sample) in pcmData.withIndex()) {
            val normalizedSample = sample / Short.MAX_VALUE.toFloat()
            if (normalizedSample > THRESHOLD && !inSwipe) {
                inSwipe = true
                startTime = index.toLong()
            } else if (normalizedSample < THRESHOLD && inSwipe) {
                inSwipe = false
                swipes.add(Swipe(startTime, index.toLong()))
            }
        }

        return swipes
    }
}
