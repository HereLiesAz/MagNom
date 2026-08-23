package com.hereliesaz.magnom.core

/** One decoded swipe: which track format matched and the recovered track string. */
data class DecodedSwipe(
    val format: TrackFormat,
    val track: String,
)

/**
 * Recovers a track string from recorded F2F audio (a coil or audio-jack read head, or a
 * generated waveform played back). The pipeline is: detect flux transitions → measure the
 * intervals between them → recover bits with an adaptive clock → decode with [TrackCodec].
 *
 * Two robustness properties the previous decoder lacked:
 *  - the clock reference adapts continuously, so it tracks the speed changes within a real
 *    hand swipe rather than assuming a fixed samples-per-bit; and
 *  - a short interval only forms a `1` when it is *paired* with a second short interval —
 *    an unpaired short interval is treated as noise/desync instead of being assumed valid.
 *
 * Both swipe directions are attempted (a card can be swiped either way), and both track
 * formats are tried, so the caller just gets whatever validly decodes.
 */
object SwipeDecoder {

    fun decode(pcm: FloatArray): DecodedSwipe? {
        val transitions = detectTransitions(pcm)
        if (transitions.size < 8) return null
        val intervals = IntArray(transitions.size - 1) { transitions[it + 1] - transitions[it] }

        for (seq in listOf(intervals.toList(), intervals.reversed())) {
            val bits = intervalsToBits(seq) ?: continue
            for (format in listOf(TrackFormat.TRACK2, TrackFormat.TRACK1)) {
                TrackCodec.decodeBits(bits, format)?.let { return DecodedSwipe(format, it) }
            }
        }
        return null
    }

    /**
     * Detect flux transitions as threshold-crossing peaks. The signal from a read head is
     * a train of alternating-polarity pulses; each pulse peak is one transition. We track a
     * running amplitude to set an adaptive threshold and emit a transition at each local
     * extreme that clears it, requiring a polarity change between successive transitions.
     */
    private fun detectTransitions(pcm: FloatArray): IntArray {
        if (pcm.isEmpty()) return IntArray(0)
        var peak = 0f
        for (v in pcm) { val a = if (v < 0) -v else v; if (a > peak) peak = a }
        if (peak == 0f) return IntArray(0)
        val threshold = peak * 0.30f

        val out = ArrayList<Int>()
        var lookingForPositive = pcm[0] < 0
        var extremeIdx = -1
        var extremeVal = 0f
        for (i in pcm.indices) {
            val v = pcm[i]
            if (lookingForPositive) {
                if (v > threshold) {
                    if (extremeIdx < 0 || v > extremeVal) { extremeVal = v; extremeIdx = i }
                    if (v < extremeVal * 0.7f) { out.add(extremeIdx); lookingForPositive = false; extremeIdx = -1; extremeVal = 0f }
                } else if (extremeIdx >= 0) {
                    out.add(extremeIdx); lookingForPositive = false; extremeIdx = -1; extremeVal = 0f
                }
            } else {
                if (v < -threshold) {
                    if (extremeIdx < 0 || v < extremeVal) { extremeVal = v; extremeIdx = i }
                    if (v > extremeVal * 0.7f) { out.add(extremeIdx); lookingForPositive = true; extremeIdx = -1; extremeVal = 0f }
                } else if (extremeIdx >= 0) {
                    out.add(extremeIdx); lookingForPositive = true; extremeIdx = -1; extremeVal = 0f
                }
            }
        }
        if (extremeIdx >= 0) out.add(extremeIdx)
        return out.toIntArray()
    }

    /**
     * Convert transition intervals to bits with an adaptive clock. A long interval is a `0`;
     * a pair of short intervals is a `1`. The reference cell width is seeded from the first
     * few intervals (the leading zeros) and nudged toward the observed cell width as it goes.
     */
    private fun intervalsToBits(intervals: List<Int>): List<Int>? {
        if (intervals.size < 4) return null
        // Seed reference from the median of the first several intervals (leading zeros are full cells).
        val seed = intervals.take(6).sorted()
        var reference = seed[seed.size / 2].toFloat()
        if (reference <= 0f) return null

        val bits = ArrayList<Int>()
        var i = 0
        while (i < intervals.size) {
            val d = intervals[i]
            val isShort = d < reference * 0.75f
            if (isShort) {
                // A one requires a matching second short interval.
                if (i + 1 >= intervals.size) break
                val d2 = intervals[i + 1]
                if (d2 >= reference * 0.75f) return null // unpaired short → desync/noise
                bits.add(1)
                reference = reference * 0.75f + (d + d2).toFloat() * 0.25f
                i += 2
            } else {
                bits.add(0)
                reference = reference * 0.75f + d.toFloat() * 0.25f
                i += 1
            }
        }
        return bits
    }
}
