package com.hereliesaz.magnom.transport

/**
 * Plays raw mono float PCM. Android drives an AudioTrack; desktop drives a
 * javax.sound SourceDataLine. Kept as an [expect] so the F2F audio transport itself lives
 * once in common code.
 */
expect class AudioSink() {
    /** Play [pcm] at [sampleRate] Hz, suspending until playback finishes. */
    suspend fun play(pcm: FloatArray, sampleRate: Int)
    fun stop()
}
