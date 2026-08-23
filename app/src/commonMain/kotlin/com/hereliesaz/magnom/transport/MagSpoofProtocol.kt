package com.hereliesaz.magnom.transport

import com.hereliesaz.magnom.domain.Card

/**
 * The MagSpoof serial control protocol, shared by every wired/serial transport.
 *
 * Grounded in the open-source Electronic Cats MagSpoof v4/v5, which present as a USB
 * CDC-ACM virtual serial port and accept a simple line protocol: prefix the track data,
 * then trigger emulation. DIY wireless builds (e.g. an HC-05 over Bluetooth Classic SPP)
 * carry exactly the same bytes, so the USB and Bluetooth transmitters share this object.
 *
 *   T1:<track1>\n     load Track 1
 *   T2:<track2>\n     load Track 2
 *   SPOOF\n           emulate the loaded tracks
 *
 * The v4/v5 "Getting Started" guide documents firmware-parser character substitutions for
 * hard-coded Track 1 data; the same constraints apply over the serial link, so they are
 * applied here.
 */
object MagSpoofProtocol {

    const val BAUD = 115_200

    /** Track 1 substitutions expected by the MagSpoof firmware parser ('&' -> '^', '-' -> '/'). */
    fun track1ForDevice(track1: String): String = track1.replace('&', '^').replace('-', '/')

    /** The ordered command frames to load a card and trigger emulation. */
    fun commands(card: Card): List<ByteArray> = listOf(
        "T1:${track1ForDevice(card.track1)}\n".encodeToByteArray(),
        "T2:${card.track2}\n".encodeToByteArray(),
        "SPOOF\n".encodeToByteArray(),
    )
}
