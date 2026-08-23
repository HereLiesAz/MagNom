package com.hereliesaz.magnom.transport

import com.hereliesaz.magnom.domain.Card
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagSpoofProtocolTest {

    @Test
    fun commands_match_the_magspoof_serial_protocol() {
        val card = Card.fromFields(
            id = "id", label = "Test", pan = "4012888888881881",
            name = "DOE/JANE", expiration = "2512", serviceCode = "101",
        ).getOrThrow()

        val frames = MagSpoofProtocol.commands(card).map { it.decodeToString() }
        assertEquals(3, frames.size)
        assertTrue(frames[0].startsWith("T1:%B") && frames[0].endsWith("\n"))
        assertTrue(frames[1].startsWith("T2:;") && frames[1].endsWith("\n"))
        assertEquals("SPOOF\n", frames[2])
    }

    @Test
    fun track1_hyphens_are_substituted_for_the_firmware_parser() {
        assertEquals("%BDOE/SMITH^", MagSpoofProtocol.track1ForDevice("%BDOE-SMITH^"))
        assertEquals("A^B", MagSpoofProtocol.track1ForDevice("A&B"))
    }
}
