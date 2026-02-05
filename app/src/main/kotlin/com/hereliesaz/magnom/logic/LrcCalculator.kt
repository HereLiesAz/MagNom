package com.hereliesaz.magnom.logic

/**
 * Utility for calculating and validating Longitudinal Redundancy Check (LRC) characters.
 *
 * LRC is a validity check byte appended to the end of a magnetic stripe track.
 * It is calculated by XORing all preceding characters in the message.
 */
object LrcCalculator {
    /**
     * Calculates the Longitudinal Redundancy Check (LRC) for a given track string.
     * The LRC is calculated by performing a bitwise XOR on all characters of the track data.
     *
     * @param trackData The string content of the track (usually from start sentinel to end sentinel).
     * @return The calculated LRC character.
     */
    fun calculate(trackData: String): Char {
        val lrc = trackData.fold(0) { acc, char -> acc xor char.code }
        return lrc.toChar()
    }

    /**
     * Validates a track string by checking its LRC.
     * It calculates the LRC of the data (all but the last character) and compares it
     * to the LRC character present in the track (the last character).
     *
     * @param trackWithLrc The full track string including the LRC character at the end.
     * @return True if the LRC is valid, false otherwise.
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
