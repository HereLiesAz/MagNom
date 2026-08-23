package com.hereliesaz.magnom.domain

import com.hereliesaz.magnom.core.TrackCodec
import com.hereliesaz.magnom.core.TrackFormat
import com.hereliesaz.magnom.core.Lrc
import kotlinx.serialization.Serializable

/**
 * A stored magnetic stripe card profile.
 *
 * The core invariant of the whole app lives here: **a Card cannot exist without valid,
 * LRC-checked Track 1 and Track 2 strings.** The primary constructor is private, so the
 * only ways to obtain a Card are [fromFields] (which generates the tracks) and
 * [fromRawTracks] (which parses and validates supplied tracks). This is what makes the
 * old "save a card, transmit empty bytes over BLE" failure structurally impossible: there
 * is no code path that yields a Card with empty or malformed track data.
 */
@Serializable
data class Card private constructor(
    val id: String,
    val label: String,
    val pan: String,
    val name: String,
    val expiration: String,
    val serviceCode: String,
    val track1: String,
    val track2: String,
    val notes: List<String> = emptyList(),
) {
    companion object {
        /**
         * Build a Card from human-entered fields, generating both tracks. Returns a failure
         * (never a half-populated Card) if the fields are not encodable.
         */
        fun fromFields(
            id: String,
            label: String,
            pan: String,
            name: String,
            expiration: String,
            serviceCode: String,
            notes: List<String> = emptyList(),
        ): Result<Card> = runCatching {
            val track1 = TrackCodec.generateTrack1(pan, name.uppercase(), expiration, serviceCode)
            val track2 = TrackCodec.generateTrack2(pan, expiration, serviceCode)
            Card(id, label.ifBlank { "Card ${pan.takeLast(4)}" }, pan, name, expiration, serviceCode, track1, track2, notes)
        }

        /**
         * Build a Card from raw track strings (the advanced/raw analyzer path). Both tracks
         * must carry a valid LRC and parse cleanly, or a failure is returned.
         */
        fun fromRawTracks(
            id: String,
            label: String,
            track1: String,
            track2: String,
            notes: List<String> = emptyList(),
        ): Result<Card> = runCatching {
            require(Lrc.validate(track1, TrackFormat.TRACK1)) { "Track 1 LRC/format invalid" }
            require(Lrc.validate(track2, TrackFormat.TRACK2)) { "Track 2 LRC/format invalid" }
            val t2 = TrackCodec.parseTrack2(track2) ?: error("Track 2 could not be parsed")
            val t1 = TrackCodec.parseTrack1(track1)
            Card(
                id = id,
                label = label.ifBlank { "Card ${t2.pan.takeLast(4)}" },
                pan = t2.pan,
                name = t1?.name ?: "",
                expiration = t2.expiration,
                serviceCode = t2.serviceCode,
                track1 = track1,
                track2 = track2,
                notes = notes,
            )
        }
    }
}
