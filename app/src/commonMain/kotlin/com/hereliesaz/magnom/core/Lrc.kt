package com.hereliesaz.magnom.core

/**
 * The Longitudinal Redundancy Check character for a magnetic stripe track.
 *
 * Per ISO/IEC 7811 the LRC is an even column parity over the *data values* of every
 * character from the start sentinel through the end sentinel, inclusive (as documented
 * by MagTek's card-standards reference). Each LRC bit column, taken together with the
 * corresponding columns of all preceding characters, has even parity; the LRC character
 * then carries its own odd parity bit like any other character (handled by [TrackCodec]).
 *
 * The previous implementation XORed raw ASCII codes, which left the shared high bits of
 * the base offset dangling and produced control characters outside the track alphabet —
 * corrupting the checksum and crashing the waveform generator. Working on data values
 * (ASCII minus [TrackFormat.baseCode]) and re-adding the base is what keeps the result a
 * legal track character and keeps this identical to the bit-level LRC used by the codec.
 */
object Lrc {

    /** The data value of [c] for [format] (ASCII minus base). Throws if out of range. */
    fun valueOf(c: Char, format: TrackFormat): Int {
        val v = c.code - format.baseCode
        require(v in 0..format.maxValue) { "Character '$c' (0x${c.code.toString(16)}) is not valid for $format" }
        return v
    }

    /**
     * Compute the LRC character for [bodyWithSentinels] — a track string that runs from
     * the start sentinel through the end sentinel, without the LRC character itself.
     */
    fun compute(bodyWithSentinels: String, format: TrackFormat): Char {
        var acc = 0
        for (c in bodyWithSentinels) acc = acc xor valueOf(c, format)
        return (acc + format.baseCode).toChar()
    }

    /**
     * Validate a complete track string (start sentinel … end sentinel, then LRC char).
     * Returns false rather than throwing on any malformed input.
     */
    fun validate(fullTrack: String, format: TrackFormat): Boolean {
        if (fullTrack.length < 3) return false
        return try {
            compute(fullTrack.dropLast(1), format) == fullTrack.last()
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
