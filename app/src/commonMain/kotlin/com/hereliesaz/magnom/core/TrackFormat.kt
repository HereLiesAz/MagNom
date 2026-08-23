package com.hereliesaz.magnom.core

/**
 * The three magnetic stripe track formats defined by ISO/IEC 7811 and 7813.
 *
 * Each track encodes characters using a fixed number of bits: [dataBits] data bits
 * (least-significant bit recorded first) plus one odd-parity bit. A character's data
 * value is its ASCII code minus [baseCode], so the on-stripe alphabet is a contiguous
 * range starting at [baseCode].
 *
 * This enum is the single source of truth for every encoding constant in the app; the
 * codec, the LRC, the F2F waveform generator and the swipe decoder all derive their
 * behaviour from it, which is what keeps encode and decode provably symmetric.
 */
enum class TrackFormat(
    val dataBits: Int,
    val baseCode: Int,
    val startSentinel: Char,
    val endSentinel: Char,
    val fieldSeparator: Char,
    /** Maximum number of data characters between the sentinels (excludes SS, ES and LRC). */
    val maxDataChars: Int,
) {
    /** Track 1 (IATA), Format B: 7-bit, 6 data bits, alphanumeric, offset from space (0x20). */
    TRACK1(dataBits = 6, baseCode = 0x20, startSentinel = '%', endSentinel = '?', fieldSeparator = '^', maxDataChars = 76),

    /** Track 2 (ABA): 5-bit, 4 data bits, numeric, offset from '0' (0x30). */
    TRACK2(dataBits = 4, baseCode = 0x30, startSentinel = ';', endSentinel = '?', fieldSeparator = '=', maxDataChars = 37),

    /** Track 3 (THRIFT): 5-bit, 4 data bits, numeric, offset from '0' (0x30). */
    TRACK3(dataBits = 4, baseCode = 0x30, startSentinel = ';', endSentinel = '?', fieldSeparator = '=', maxDataChars = 104);

    /** Total bits per character, including the parity bit. */
    val totalBits: Int get() = dataBits + 1

    /** Highest data value representable in [dataBits]. */
    val maxValue: Int get() = (1 shl dataBits) - 1

    /** True if [c] is a legal character for this track (in-range once the base is removed). */
    fun accepts(c: Char): Boolean = (c.code - baseCode) in 0..maxValue

    companion object {
        /** Infer the track format from a raw track string by its start sentinel. */
        fun of(track: String): TrackFormat = when (track.firstOrNull()) {
            TRACK1.startSentinel -> TRACK1
            else -> TRACK2
        }
    }
}
