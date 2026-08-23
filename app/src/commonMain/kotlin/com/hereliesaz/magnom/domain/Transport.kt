package com.hereliesaz.magnom.domain

import kotlinx.coroutines.flow.StateFlow

/** The physical channel a [Transmitter] drives. */
enum class TransportKind { AUDIO, BLUETOOTH, USB }

/** Lifecycle of a transmitter. */
enum class TransportStatus { UNAVAILABLE, READY, CONNECTING, CONNECTED, TRANSMITTING }

/** Outcome of a transmit attempt. */
sealed interface TransmitResult {
    data object Success : TransmitResult
    data class Failure(val message: String) : TransmitResult
}

/**
 * A way to push a card's track data to a magnetic-stripe reader — an F2F waveform through
 * the audio jack / coil, a BLE peripheral, or a USB device.
 *
 * Every transmitter takes a whole [Card], never loose strings, so the tracks it sends are
 * the ones the Card invariant already guarantees are valid and non-empty. This is the
 * structural end of the old "BLE writes zero-length byte arrays" bug: there is no API here
 * that could be handed empty track data.
 */
interface Transmitter {
    val kind: TransportKind
    val status: StateFlow<TransportStatus>
    /** A human label for the currently targeted device, or null when none is selected. */
    val target: StateFlow<String?>
    /** True when this transport can be used on this device right now. */
    fun isAvailable(): Boolean
    suspend fun transmit(card: Card): TransmitResult
    fun release()
}
