package com.hereliesaz.magnom.transport

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.TransmitResult
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.domain.TransportKind
import com.hereliesaz.magnom.domain.TransportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Transmits a card to a MagSpoof-class BLE peripheral. Scans for the custom GATT service,
 * connects, writes Track 1 and Track 2, then issues the emulate command on the control
 * point. Because it only accepts a whole [Card], the tracks it writes are always valid.
 */
class BleTransmitter(private val context: Context) : Transmitter {

    override val kind = TransportKind.BLE

    private val _status = MutableStateFlow(TransportStatus.READY)
    override val status: StateFlow<TransportStatus> = _status.asStateFlow()

    private val _target = MutableStateFlow<String?>(null)
    override val target: StateFlow<String?> = _target.asStateFlow()

    private val manager get() = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = manager?.adapter

    override fun isAvailable(): Boolean =
        adapter?.isEnabled == true && hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    @SuppressLint("MissingPermission")
    override suspend fun transmit(card: Card): TransmitResult {
        val adapter = adapter ?: return TransmitResult.Failure("Bluetooth unavailable")
        if (!isAvailable()) return TransmitResult.Failure("Bluetooth permission or radio off")

        _status.value = TransportStatus.CONNECTING
        val device = withTimeoutOrNull(SCAN_TIMEOUT_MS) { scanForPeripheral(adapter) }
            ?: run { _status.value = TransportStatus.READY; return TransmitResult.Failure("No MagSpoof peripheral found") }
        _target.value = device.address

        val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) { connectAndWrite(device, card) }
        _status.value = TransportStatus.READY
        return result ?: TransmitResult.Failure("BLE transfer timed out")
    }

    @SuppressLint("MissingPermission")
    private suspend fun scanForPeripheral(adapter: BluetoothAdapter): BluetoothDevice? =
        suspendCancellableCoroutine { cont ->
            val scanner = adapter.bluetoothLeScanner ?: run { cont.resume(null); return@suspendCancellableCoroutine }
            val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
            val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            val cb = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    runCatching { scanner.stopScan(this) }
                    if (cont.isActive) cont.resume(result.device)
                }
            }
            scanner.startScan(listOf(filter), settings, cb)
            cont.invokeOnCancellation { runCatching { scanner.stopScan(cb) } }
        }

    @SuppressLint("MissingPermission")
    private suspend fun connectAndWrite(device: BluetoothDevice, card: Card): TransmitResult =
        suspendCancellableCoroutine { cont ->
            val queue = ArrayDeque<Pair<UUID, ByteArray>>()
            var gattRef: BluetoothGatt? = null

            fun finish(result: TransmitResult) {
                runCatching { gattRef?.disconnect(); gattRef?.close() }
                if (cont.isActive) cont.resume(result)
            }

            @Suppress("DEPRECATION")
            fun BluetoothGatt.legacyWrite(ch: BluetoothGattCharacteristic, value: ByteArray) {
                ch.value = value
                writeCharacteristic(ch)
            }

            fun writeNext(gatt: BluetoothGatt, service: android.bluetooth.BluetoothGattService) {
                val (uuid, value) = queue.removeFirst()
                val ch = service.getCharacteristic(uuid) ?: return finish(TransmitResult.Failure("Characteristic $uuid missing"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    gatt.legacyWrite(ch, value)
                }
            }

            val callback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) gatt.discoverServices()
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) finish(TransmitResult.Failure("Disconnected"))
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    val service = gatt.getService(SERVICE_UUID)
                        ?: return finish(TransmitResult.Failure("MagSpoof service not found"))
                    queue.addLast(TRACK1_UUID to card.track1.encodeToByteArray())
                    queue.addLast(TRACK2_UUID to card.track2.encodeToByteArray())
                    queue.addLast(CONTROL_UUID to byteArrayOf(CMD_EMULATE))
                    writeNext(gatt, service)
                }

                override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) return finish(TransmitResult.Failure("Write failed"))
                    val service = gatt.getService(SERVICE_UUID) ?: return finish(TransmitResult.Failure("Service lost"))
                    if (queue.isEmpty()) finish(TransmitResult.Success) else writeNext(gatt, service)
                }
            }

            gattRef = device.connectGatt(context, false, callback)
            cont.invokeOnCancellation { runCatching { gattRef?.close() } }
        }

    override fun release() { _status.value = TransportStatus.READY }

    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    companion object {
        // Custom MagSpoof GATT service. Peripheral firmware must expose these.
        private val SERVICE_UUID = UUID.fromString("0000aa01-0000-1000-8000-00805f9b34fb")
        private val TRACK1_UUID = UUID.fromString("0000aa02-0000-1000-8000-00805f9b34fb")
        private val TRACK2_UUID = UUID.fromString("0000aa03-0000-1000-8000-00805f9b34fb")
        private val CONTROL_UUID = UUID.fromString("0000aa04-0000-1000-8000-00805f9b34fb")
        private const val CMD_EMULATE: Byte = 0x01
        private const val SCAN_TIMEOUT_MS = 8_000L
        private const val CONNECT_TIMEOUT_MS = 12_000L
    }
}
