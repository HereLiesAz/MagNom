package com.hereliesaz.magnom.core

/** The structured contents of a parsed Track 1 string. */
data class Track1Fields(
    val pan: String,
    val name: String,
    val expiration: String,
    val serviceCode: String,
    val discretionary: String,
)

/** The structured contents of a parsed Track 2 string. */
data class Track2Fields(
    val pan: String,
    val expiration: String,
    val serviceCode: String,
    val discretionary: String,
)

/**
 * Generates and parses ISO/IEC 7811/7813 track strings, and converts complete track
 * strings to and from the bit stream that the F2F waveform layer records.
 *
 * All four operations share [Lrc] and [TrackFormat], so a string this object generates
 * always decodes back to itself — the property the test suite pins with real card
 * vectors instead of the implementation checking against itself.
 */
object TrackCodec {

    // ---- Track string generation -------------------------------------------------

    /** Build a complete Track 1 (Format B) string, LRC appended. */
    fun generateTrack1(
        pan: String,
        name: String,
        expiration: String,
        serviceCode: String,
        discretionary: String = "",
    ): String {
        requirePan(pan)
        require(name.length <= 26) { "Track 1 name must be ≤ 26 characters" }
        requireExpiration(expiration)
        requireServiceCode(serviceCode)
        val body = "%B$pan${TrackFormat.TRACK1.fieldSeparator}$name" +
            "${TrackFormat.TRACK1.fieldSeparator}$expiration$serviceCode$discretionary?"
        requireAlphabet(body, TrackFormat.TRACK1)
        return body + Lrc.compute(body, TrackFormat.TRACK1)
    }

    /** Build a complete Track 2 (ABA) string, LRC appended. */
    fun generateTrack2(
        pan: String,
        expiration: String,
        serviceCode: String,
        discretionary: String = "",
    ): String {
        requirePan(pan)
        requireExpiration(expiration)
        requireServiceCode(serviceCode)
        val body = ";$pan${TrackFormat.TRACK2.fieldSeparator}$expiration$serviceCode$discretionary?"
        requireAlphabet(body, TrackFormat.TRACK2)
        return body + Lrc.compute(body, TrackFormat.TRACK2)
    }

    // ---- Track string parsing ----------------------------------------------------

    /** Parse a complete Track 1 string, or null if malformed or the LRC does not check. */
    fun parseTrack1(track: String): Track1Fields? {
        val f = TrackFormat.TRACK1
        if (track.length < 4 || track[0] != f.startSentinel || track[track.length - 2] != f.endSentinel) return null
        if (!Lrc.validate(track, f)) return null
        val body = track.substring(1, track.length - 2) // between SS and ES, without format-code stripping yet
        // body begins with the single-letter format code (e.g. 'B')
        if (body.isEmpty()) return null
        val afterFormat = body.substring(1)
        val fields = afterFormat.split(f.fieldSeparator)
        if (fields.size < 3) return null
        val pan = fields[0]
        val name = fields[1]
        val trailer = fields[2]
        if (pan.isEmpty() || pan.length > 19 || trailer.length < 7) return null
        return Track1Fields(
            pan = pan,
            name = name,
            expiration = trailer.substring(0, 4),
            serviceCode = trailer.substring(4, 7),
            discretionary = trailer.substring(7),
        )
    }

    /** Parse a complete Track 2 string, or null if malformed or the LRC does not check. */
    fun parseTrack2(track: String): Track2Fields? {
        val f = TrackFormat.TRACK2
        if (track.length < 4 || track[0] != f.startSentinel || track[track.length - 2] != f.endSentinel) return null
        if (!Lrc.validate(track, f)) return null
        val body = track.substring(1, track.length - 2)
        val parts = body.split(f.fieldSeparator)
        if (parts.size != 2) return null
        val pan = parts[0]
        val trailer = parts[1]
        if (pan.isEmpty() || pan.length > 19 || trailer.length < 7) return null
        return Track2Fields(
            pan = pan,
            expiration = trailer.substring(0, 4),
            serviceCode = trailer.substring(4, 7),
            discretionary = trailer.substring(7),
        )
    }

    // ---- Bit-level encoding (used by the F2F waveform layer) ----------------------

    /**
     * Encode a complete track string (including its LRC character) into a bit list,
     * least-significant data bit first, one odd-parity bit per character.
     */
    fun encodeBits(fullTrack: String, format: TrackFormat = TrackFormat.of(fullTrack)): List<Int> {
        val bits = ArrayList<Int>(fullTrack.length * format.totalBits)
        for (c in fullTrack) appendChar(bits, Lrc.valueOf(c, format), format)
        return bits
    }

    /**
     * Decode a bit list back into the track string (start sentinel … end sentinel …
     * LRC character), verifying per-character odd parity and the trailing LRC. Returns
     * null if no aligned, parity-clean, LRC-valid frame is found.
     *
     * The bit list may contain arbitrary leading/trailing zeros; decoding slides to the
     * first alignment at which the start-sentinel pattern appears on a character boundary.
     */
    fun decodeBits(bits: List<Int>, format: TrackFormat): String? {
        val startPattern = ArrayList<Int>(format.totalBits).also { appendChar(it, Lrc.valueOf(format.startSentinel, format), format) }
        var offset = 0
        while (offset + startPattern.size <= bits.size) {
            if (matchesAt(bits, offset, startPattern)) {
                decodeFrom(bits, offset, format)?.let { return it }
            }
            offset++
        }
        return null
    }

    private fun decodeFrom(bits: List<Int>, start: Int, format: TrackFormat): String? {
        val sb = StringBuilder()
        var idx = start
        var lrcAcc = 0
        while (idx + format.totalBits <= bits.size) {
            val value = decodeChar(bits, idx, format) ?: return null
            idx += format.totalBits
            if (value == Lrc.valueOf(format.endSentinel, format)) {
                sb.append(format.endSentinel)
                lrcAcc = lrcAcc xor value
                // Next character must be the LRC.
                val lrcValue = decodeChar(bits, idx, format) ?: return null
                if (lrcValue != lrcAcc) return null
                sb.append((lrcValue + format.baseCode).toChar())
                return sb.toString()
            }
            sb.append((value + format.baseCode).toChar())
            lrcAcc = lrcAcc xor value
        }
        return null
    }

    /** Decode one character starting at [pos], returning its data value or null on parity failure. */
    private fun decodeChar(bits: List<Int>, pos: Int, format: TrackFormat): Int? {
        if (pos + format.totalBits > bits.size) return null
        var value = 0
        var ones = 0
        for (i in 0 until format.dataBits) {
            val b = bits[pos + i]
            value = value or (b shl i)
            ones += b
        }
        ones += bits[pos + format.dataBits] // parity bit
        return if (ones % 2 == 1) value else null // odd parity required
    }

    private fun appendChar(out: MutableList<Int>, value: Int, format: TrackFormat) {
        var ones = 0
        for (i in 0 until format.dataBits) {
            val b = (value shr i) and 1
            out.add(b); ones += b
        }
        out.add((ones + 1) and 1) // odd parity
    }

    private fun matchesAt(bits: List<Int>, offset: Int, pattern: List<Int>): Boolean {
        for (i in pattern.indices) if (bits[offset + i] != pattern[i]) return false
        return true
    }

    // ---- Input validation --------------------------------------------------------

    private fun requirePan(pan: String) {
        require(pan.isNotEmpty() && pan.length <= 19 && pan.all { it.isDigit() }) { "PAN must be 1–19 digits" }
    }

    private fun requireExpiration(exp: String) {
        require(exp.length == 4 && exp.all { it.isDigit() }) { "Expiration must be 4 digits (YYMM)" }
    }

    private fun requireServiceCode(svc: String) {
        require(svc.length == 3 && svc.all { it.isDigit() }) { "Service code must be 3 digits" }
    }

    private fun requireAlphabet(body: String, format: TrackFormat) {
        val bad = body.firstOrNull { !format.accepts(it) }
        require(bad == null) { "Character '$bad' is not encodable on $format" }
    }
}
