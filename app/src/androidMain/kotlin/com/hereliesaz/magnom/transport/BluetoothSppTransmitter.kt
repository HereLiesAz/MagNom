package com.hereliesaz.magnom.transport

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
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

    override fun isAvailable(): Boolean =
        adapter?.isEnabled == true && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    @SuppressLint("MissingPermission")
    override suspend fun transmit(card: Card): TransmitResult = withContext(Dispatchers.IO) {
        val adapter = adapter ?: return@withContext TransmitResult.Failure("Bluetooth unavailable")
        if (!isAvailable()) return@withContext TransmitResult.Failure("Bluetooth off or permission not granted")

        val device = pickDevice(adapter)
            ?: return@withContext TransmitResult.Failure("No paired MagSpoof/serial device. Pair one in Bluetooth settings.")
        _target.value = device.name ?: device.address

        var socket: BluetoothSocket? = null
        try {
            _status.value = TransportStatus.CONNECTING
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            adapter.cancelDiscovery()
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
        val bonded = adapter.bondedDevices ?: emptySet()
        return bonded.firstOrNull { d ->
            val n = d.name?.lowercase() ?: ""
            NAME_HINTS.any { n.contains(it) }
        } ?: bonded.firstOrNull()
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
