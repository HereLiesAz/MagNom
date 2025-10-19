package com.hereliesaz.magnom.logic

class TrackDataGenerator {

    /**
     * Generates a Track 2 string from the provided card details.
     * Track 2 format: ;[PAN]=[YYMM][Service Code]?
     *
     * @param pan The Primary Account Number (up to 19 digits).
     * @param expirationDate The expiration date in YYMM format.
     * @param serviceCode The 3-digit service code.
     * @return The formatted Track 2 string with a calculated LRC.
     */
    fun generateTrack2(pan: String, expirationDate: String, serviceCode: String): String {
        // Basic validation
        if (pan.length > 19 || expirationDate.length != 4 || serviceCode.length != 3) {
            throw IllegalArgumentException("Invalid data for Track 2 generation.")
        }

        val trackData = ";$pan=$expirationDate$serviceCode?"
        val lrc = LrcCalculator.calculate(trackData)
        return trackData + lrc
    }
}
