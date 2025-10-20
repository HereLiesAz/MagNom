package com.hereliesaz.magnom.logic

object LrcCalculator {
    /**
     * Calculates the Longitudinal Redundancy Check (LRC) for a given track string.
     * The LRC is calculated by performing a bitwise XOR on all characters of the track data.
     */
    fun calculate(trackData: String): Char {
        val lrc = trackData.fold(0) { acc, char -> acc xor char.code }
        return lrc.toChar()
    }

    /**
     * Validates a track string by checking its LRC.
     * It calculates the LRC of the data (all but the last character) and compares it
     * to the LRC character present in the track (the last character).
     */
    fun validate(trackWithLrc: String): Boolean {
        if (trackWithLrc.isEmpty()) {
            return false
        }
        val data = trackWithLrc.dropLast(1)
        val lrcFromTrack = trackWithLrc.last()
        val calculatedLrc = calculate(data)
        return lrcFromTrack == calculatedLrc
    }
}
