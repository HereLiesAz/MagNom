package com.hereliesaz.magnom.domain

import com.hereliesaz.magnom.core.TrackCodec
import com.hereliesaz.magnom.core.Wav
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CardInvariantTest {
    private val t1 = TrackCodec.generateTrack1("4111111111111111", "DOE/JOHN", "3012", "101")
    private val t2 = TrackCodec.generateTrack2("4111111111111111", "3012", "101")

    @Test fun raw_tracks_with_matching_pan_are_accepted_and_consistent() {
        val card = Card.fromRawTracks("1", "", t1, t2).getOrThrow()
        assertTrue(card.isConsistent())
    }

    @Test fun raw_tracks_with_different_pans_are_rejected() {
        val other = TrackCodec.generateTrack2("4222222222222222", "3012", "101")
        assertTrue(Card.fromRawTracks("1", "", t1, other).isFailure)
    }

    @Test fun name_containing_a_track_delimiter_is_rejected() {
        for (bad in listOf("A^B", "A%B", "A?B")) {
            assertTrue(Card.fromFields("1", "", "4111111111111111", bad, "3012", "101").isFailure, bad)
        }
    }

    @Test fun wav_rejects_stereo_and_non_pcm() {
        val mono = Wav.encode(FloatArray(64))
        Wav.decode(mono) // sanity
        val stereo = mono.copyOf().also { it[22] = 2 }
        assertFailsWith<IllegalArgumentException> { Wav.decode(stereo) }
        val float = mono.copyOf().also { it[20] = 3 }
        assertFailsWith<IllegalArgumentException> { Wav.decode(float) }
    }

    @Test fun wav_with_negative_chunk_size_fails_instead_of_looping() {
        // Turn the "data" chunk id into an unknown one with a negative size.
        val bad = Wav.encode(FloatArray(64)).copyOf().also {
            it[36] = 'x'.code.toByte()
            it[43] = 0x80.toByte()
        }
        assertFailsWith<IllegalArgumentException> { Wav.decode(bad) }
    }
}
