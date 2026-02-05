package com.hereliesaz.magnom.logic

/**
 * Data class representing the components of a magnetic stripe track.
 */
data class ParsedTrack2Data(
    val pan: String,
    val expirationDate: String,
    val serviceCode: String
)

/**
 * Parses raw magnetic stripe track strings into structured data.
 */
class TrackDataParser {

    /**
     * Parses a raw Track 2 string and returns the structured data.
     * Track 2 format: ;[PAN]=[YYMM][Service Code]?[LRC]
     *
     * @param track2Data The raw Track 2 string.
     * @return A ParsedTrack2Data object, or null if parsing fails.
     */
    fun parseTrack2(track2Data: String): ParsedTrack2Data? {
        // 1. Verify length, Start and End Sentinels and LRC
        if (track2Data.length < 2 || track2Data.first() != ';' || track2Data[track2Data.length - 2] != '?') {
            return null
        }
        if (!LrcCalculator.validate(track2Data)) {
            return null
        }

        // 2. Split the string by the Field Separator
        val parts = track2Data.substring(1, track2Data.length - 2).split('=')
        if (parts.size != 2) {
            return null
        }

        // 3. Extract and validate the components
        val pan = parts[0]
        val remainingData = parts[1]

        if (pan.isEmpty() || pan.length > 19 || remainingData.length < 7) {
            return null
        }

        val expirationDate = remainingData.substring(0, 4)
        val serviceCode = remainingData.substring(4, 7)

        return ParsedTrack2Data(pan, expirationDate, serviceCode)
    }
}
