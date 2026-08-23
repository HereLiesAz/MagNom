package com.hereliesaz.magnom.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Standards-anchored tests for the magstripe core.
 *
 * These deliberately do NOT compute their expected values by calling the code under test
 * (the trap the old suite fell into). LRC expectations are worked out by hand from the
 * ISO/IEC 7811 even-column rule; round-trip tests assert that an independently constructed
 * standard string survives encode→decode and PCM→decode unchanged.
 */
class CodecTest {

    @Test
    fun lrc_track2_known_vector() {
        // Hand-computed: XOR of data values of ';','1','2','3','4','5','?' (SS..ES inclusive)
        //   11 ^ 1 ^ 2 ^ 3 ^ 4 ^ 5 ^ 15 = 5  ->  '0' + 5 = '5'
        assertEquals('5', Lrc.compute(";12345?", TrackFormat.TRACK2))
        assertTrue(Lrc.validate(";12345?5", TrackFormat.TRACK2))
        assertTrue(!Lrc.validate(";12345?4", TrackFormat.TRACK2))
    }

    @Test
    fun lrc_is_always_a_legal_track_character() {
        // Regression for the base-offset bug that produced control characters (e.g. '\n')
        // and crashed the waveform generator for the majority of real PANs.
        val pans = listOf("4012888888881881", "4000000000000002", "5555555555554444", "371449635398431", "6011000990139424")
        for (pan in pans) {
            val t2 = TrackCodec.generateTrack2(pan, "2512", "101")
            val lrc = t2.last()
            assertTrue(TrackFormat.TRACK2.accepts(lrc), "LRC '$lrc' out of range for PAN $pan")
            // And it must round-trip through the bit layer without throwing.
            val bits = TrackCodec.encodeBits(t2, TrackFormat.TRACK2)
            assertEquals(t2, TrackCodec.decodeBits(bits, TrackFormat.TRACK2))
        }
    }

    @Test
    fun track2_generate_parse_roundtrip() {
        val t2 = TrackCodec.generateTrack2(pan = "4012888888881881", expiration = "2512", serviceCode = "101")
        assertEquals(';', t2.first())
        val fields = assertNotNull(TrackCodec.parseTrack2(t2))
        assertEquals("4012888888881881", fields.pan)
        assertEquals("2512", fields.expiration)
        assertEquals("101", fields.serviceCode)
    }

    @Test
    fun track1_generate_parse_roundtrip() {
        val t1 = TrackCodec.generateTrack1(pan = "4012888888881881", name = "DOE/JANE", expiration = "2512", serviceCode = "101")
        assertTrue(t1.startsWith("%B"))
        val fields = assertNotNull(TrackCodec.parseTrack1(t1))
        assertEquals("4012888888881881", fields.pan)
        assertEquals("DOE/JANE", fields.name)
        assertEquals("2512", fields.expiration)
        assertEquals("101", fields.serviceCode)
    }

    @Test
    fun bit_roundtrip_track1() {
        val t1 = TrackCodec.generateTrack1("371449635398431", "DOE/JOHN", "2601", "201")
        val bits = TrackCodec.encodeBits(t1, TrackFormat.TRACK1)
        assertEquals(t1, TrackCodec.decodeBits(bits, TrackFormat.TRACK1))
    }

    @Test
    fun f2f_pcm_roundtrip_through_swipe_decoder() {
        val t2 = TrackCodec.generateTrack2("4012888888881881", "2512", "101")
        val pcm = F2f.trackToPcm(t2, TrackFormat.TRACK2, samplesPerBit = 32)
        val decoded = assertNotNull(SwipeDecoder.decode(pcm))
        assertEquals(TrackFormat.TRACK2, decoded.format)
        assertEquals(t2, decoded.track)
    }

    @Test
    fun f2f_reverse_swipe_still_decodes() {
        val t2 = TrackCodec.generateTrack2("5555555555554444", "2711", "121")
        val pcm = F2f.trackToPcm(t2, TrackFormat.TRACK2, samplesPerBit = 32)
        pcm.reverse() // simulate a card swiped the other way
        val decoded = assertNotNull(SwipeDecoder.decode(pcm))
        assertEquals(t2, decoded.track)
    }

    @Test
    fun wav_roundtrip_preserves_signal_enough_to_decode() {
        val t2 = TrackCodec.generateTrack2("6011000990139424", "2809", "101")
        val pcm = F2f.trackToPcm(t2, TrackFormat.TRACK2, samplesPerBit = 32)
        val wav = Wav.encode(pcm)
        val back = Wav.decode(wav)
        val decoded = assertNotNull(SwipeDecoder.decode(back))
        assertEquals(t2, decoded.track)
    }

    @Test
    fun parser_rejects_bad_lrc() {
        val good = TrackCodec.generateTrack2("4012888888881881", "2512", "101")
        val tampered = good.dropLast(1) + (if (good.last() == '0') '1' else '0')
        assertNull(TrackCodec.parseTrack2(tampered))
    }
}
