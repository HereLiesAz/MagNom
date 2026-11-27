package com.hereliesaz.magnom.transport

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.TransmitResult
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.domain.TransportKind
import com.hereliesaz.magnom.domain.TransportStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Transmits a card to a MagSpoof-class device over Bluetooth Classic Serial Port Profile
 * (RFCOMM) — the wireless path real DIY builds use (e.g. an HC-05/HC-06 bridging the
 * device's serial input). It sends the exact same [MagSpoofProtocol] frames as USB.
 *
 * It connects to a bonded device whose name looks like a MagSpoof/serial bridge; pairing is
 * done once in the system Bluetooth settings. Only a whole [Card] is accepted, so empty
 * track data can never be sent.
 */
class BluetoothSppTransmitter(private val context: Context) : Transmitter {

    override val kind = TransportKind.BLUETOOTH

    private val _status = MutableStateFlow(TransportStatus.READY)
    override val status: StateFlow<TransportStatus> = _status.asStateFlow()

    private val _target = MutableStateFlow<String?>(null)
    override val target: StateFlow<String?> = _target.asStateFlow()

    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    // Permission is requested at transmit time (API 31+), so availability is just "adapter is on".
    override fun isAvailable(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    override suspend fun transmit(card: Card): TransmitResult = withContext(Dispatchers.IO) {
        val adapter = adapter ?: return@withContext TransmitResult.Failure("Bluetooth unavailable")
        if (!isAvailable()) return@withContext TransmitResult.Failure("Bluetooth is off")
        if (!ensurePermission()) return@withContext TransmitResult.Failure("Bluetooth permission denied")

        val device = pickDevice(adapter)
            ?: return@withContext TransmitResult.Failure(
                "No paired device named like ${NAME_HINTS.joinToString("/")}. Pair one in Bluetooth settings " +
                    "(rename it to include one of those if needed).",
            )
        _target.value = device.name ?: device.address

        var socket: BluetoothSocket? = null
        try {
            _status.value = TransportStatus.CONNECTING
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            // No cancelDiscovery(): the app never scans, and that call needs BLUETOOTH_SCAN on API 31+.
            socket.connect()
            _status.value = TransportStatus.TRANSMITTING
            val out = socket.outputStream
            for (frame in MagSpoofProtocol.commands(card)) out.write(frame)
            out.flush()
            TransmitResult.Success
        } catch (e: Throwable) {
            TransmitResult.Failure(e.message ?: "Bluetooth transfer failed")
        } finally {
            runCatching { socket?.close() }
            _status.value = TransportStatus.READY
        }
    }

    @SuppressLint("MissingPermission")
    private fun pickDevice(adapter: BluetoothAdapter): BluetoothDevice? {
        // Only a device that looks like a MagSpoof/serial bridge. Falling back to "any paired
        // device" would write the card's track data to headphones, a car kit, anything bonded.
        val bonded = adapter.bondedDevices ?: emptySet()
        return bonded.firstOrNull { d ->
            val n = d.name?.lowercase() ?: ""
            NAME_HINTS.any { n.contains(it) }
        }
    }

    /** API 31+ needs BLUETOOTH_CONNECT at runtime; earlier versions have it from install. */
    private suspend fun ensurePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return true
        return PermissionGate.request(Manifest.permission.BLUETOOTH_CONNECT) &&
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    }

    override fun release() { _status.value = TransportStatus.READY }

    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    companion object {
        // Standard Serial Port Profile UUID (RFCOMM).
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val NAME_HINTS = listOf("magspoof", "spoof", "hc-05", "hc-06", "hc05", "hc06", "bomber")
    }
}
