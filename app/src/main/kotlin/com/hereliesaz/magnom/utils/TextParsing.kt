package com.hereliesaz.magnom.utils

object TextParsing {
    fun parseCardNumber(text: String): String? {
        val regex = Regex("""(\d{4}[- ]?){3}\d{4}""")
        return regex.find(text)?.value?.replace(Regex("""[- ]"""), "")
    }

    fun parseExpirationDate(text: String): String? {
        val regex = Regex("""(0[1-9]|1[0-2])/\d{2}""")
        return regex.find(text)?.value
    }

    fun parseCardholderName(text: String): String? {
        val regex = Regex("""([A-Z.']+\s?)+""")
        return regex.find(text)?.value
    }
}
