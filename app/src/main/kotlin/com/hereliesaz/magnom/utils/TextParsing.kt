package com.hereliesaz.magnom.utils

/**
 * Utility object for parsing structured data from raw text strings.
 *
 * Primarily used by the OCR features to extract card details from recognized text.
 */
object TextParsing {

    /**
     * Extracts a probable card number (PAN) from the text.
     *
     * Looks for a sequence of 13-19 digits, possibly separated by spaces or dashes.
     *
     * @param text The raw text to search.
     * @return The sanitized PAN (digits only), or null if not found.
     */
    fun parseCardNumber(text: String): String? {
        // Regex for standard 16-digit cards (grouped in 4s)
        val regex = Regex("""(\d{4}[- ]?){3}\d{4}""")
        // Find match and remove separators
        return regex.find(text)?.value?.replace(Regex("""[- ]"""), "")
    }

    /**
     * Extracts a probable expiration date.
     *
     * Looks for patterns like MM/YY.
     *
     * @param text The raw text to search.
     * @return The expiration date string, or null.
     */
    fun parseExpirationDate(text: String): String? {
        // Regex matches 01-12 followed by slash and 2 digits
        val regex = Regex("""(0[1-9]|1[0-2])/\d{2}""")
        return regex.find(text)?.value
    }

    /**
     * Extracts a probable cardholder name.
     *
     * Looks for sequences of uppercase letters that might resemble a name.
     * This is a heuristic and may produce false positives.
     *
     * @param text The raw text to search.
     * @return The name string, or null.
     */
    fun parseCardholderName(text: String): String? {
        // Matches sequences of capital letters, dots, or apostrophes
        val regex = Regex("""([A-Z.']+\s?)+""")
        return regex.find(text)?.value
    }
}
