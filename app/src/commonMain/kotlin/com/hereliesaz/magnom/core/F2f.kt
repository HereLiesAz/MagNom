package com.hereliesaz.magnom.core

/**
 * F2F (Aiken biphase) modulation between a magnetic-stripe bit stream and PCM audio.
 *
 * Every bit cell begins with a flux transition (the clock). A `1` adds a second
 * transition at the middle of the cell; a `0` does not. A decoder therefore sees one
 * long interval for a `0` and two short intervals for a `1`. This is the physical layer
 * the peripheral firmware reproduces; on the phone the same waveform can drive a coil or
 * an audio-jack head directly.
 */
object F2f {

    const val DEFAULT_SAMPLE_RATE = 44_100

    /**
     * Render a bit stream to mono float PCM in the range [-1, 1].
     *
     * @param leadingZeros clock-training zero bits prepended so a reader can lock on.
     */
    fun bitsToPcm(
        bits: List<Int>,
        samplesPerBit: Int = 28,
        leadingZeros: Int = 20,
        trailingZeros: Int = 20,
    ): FloatArray {
        val full = ArrayList<Int>(bits.size + leadingZeros + trailingZeros)
        repeat(leadingZeros) { full.add(0) }
        full.addAll(bits)
        repeat(trailingZeros) { full.add(0) }

        val pcm = FloatArray(full.size * samplesPerBit)
        var level = 1f
        var head = 0
        val half = samplesPerBit / 2
        for (bit in full) {
            level = -level // clock transition at the cell boundary
            if (bit == 1) {
                repeat(half) { pcm[head++] = level }
                level = -level // mid-cell transition marks a one
                repeat(samplesPerBit - half) { pcm[head++] = level }
            } else {
                repeat(samplesPerBit) { pcm[head++] = level }
            }
        }
        return pcm
    }

    /** Convenience: encode a full track string straight to PCM. */
    fun trackToPcm(
        fullTrack: String,
        format: TrackFormat = TrackFormat.of(fullTrack),
        samplesPerBit: Int = 28,
    ): FloatArray = bitsToPcm(TrackCodec.encodeBits(fullTrack, format), samplesPerBit)
}
