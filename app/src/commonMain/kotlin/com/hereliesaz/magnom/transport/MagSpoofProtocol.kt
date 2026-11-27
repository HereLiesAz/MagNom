package com.hereliesaz.magnom.transport

import com.hereliesaz.magnom.domain.Card

/**
 * The MagSpoof serial control protocol, shared by every wired/serial transport.
 *
 * A **proposed** line protocol, not one verified against shipping firmware. Stock MagSpoof
 * v4/v5 firmware presents as a USB CDC-ACM serial port but does not document these commands;
 * this protocol assumes firmware (stock-modified or custom) that implements them. DIY wireless
 * builds (e.g. an HC-05 over Bluetooth Classic SPP) carry the same bytes, so the USB and
 * Bluetooth transmitters share this object.
 *
 *   T1:<track1>\n     load Track 1
 *   T2:<track2>\n     load Track 2
 *   SPOOF\n           emulate the loaded tracks
 *
 * The v4/v5 "Getting Started" guide documents firmware-parser character substitutions for
 * hard-coded Track 1 data; they are applied here on the assumption the same parser is used.
 * Note this means the Track 1 sent differs from the stored, LRC-checked one (the device is
 * expected to recompute its own LRC).
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
