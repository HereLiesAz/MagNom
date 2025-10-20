package com.hereliesaz.magnom

import com.hereliesaz.magnom.logic.TrackDataGenerator
import org.junit.Test
import org.junit.Assert.*

class TrackDataGeneratorTest {

    private val trackDataGenerator = TrackDataGenerator()

    @Test
    fun `generateTrack1 with valid data returns correct format`() {
        val pan = "1234567890123456"
        val name = "JOHN DOE"
        val expirationDate = "2512"
        val serviceCode = "101"
        val track1 = trackDataGenerator.generateTrack1(pan, name, expirationDate, serviceCode)
        val expectedTrackData = "%B$pan^$name^$expirationDate$serviceCode?"
        val expectedLrc = com.hereliesaz.magnom.logic.LrcCalculator.calculate(expectedTrackData)
        assertEquals("$expectedTrackData$expectedLrc", track1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateTrack1 with invalid PAN throws exception`() {
        trackDataGenerator.generateTrack1("12345678901234567890", "JOHN DOE", "2512", "101")
    }

    @Test
    fun `generateTrack2 with valid data returns correct format`() {
        val pan = "1234567890123456"
        val expirationDate = "2512"
        val serviceCode = "101"
        val track2 = trackDataGenerator.generateTrack2(pan, expirationDate, serviceCode)
        val expectedTrackData = ";$pan=$expirationDate$serviceCode?"
        val expectedLrc = com.hereliesaz.magnom.logic.LrcCalculator.calculate(expectedTrackData)
        assertEquals("$expectedTrackData$expectedLrc", track2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateTrack2 with invalid expiration date throws exception`() {
        trackDataGenerator.generateTrack2("1234567890123456", "251", "101")
    }
}
