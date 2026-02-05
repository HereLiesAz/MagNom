package com.hereliesaz.magnom.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A Foreground Service for managing Bluetooth Low Energy (BLE) communication.
 *
 * This service persists in the background (foreground notification) to maintain
 * connection state with the peripheral hardware. It handles scanning, connecting,
 * service discovery, and reliable characteristic writing via a queue.
 */
class BleCommunicationService : Service() {

    private val binder = LocalBinder()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    // StateFlows for UI observation
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _transmissionStatus = MutableStateFlow("Idle")
    val transmissionStatus: StateFlow<String> = _transmissionStatus.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    // UUIDs for the custom MagSpoof Service
    private val magSpoofServiceUUID = UUID.fromString("0000aaaa-0000-1000-8000-00805f9b34fb")
    private val track1CharacteristicUUID = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb")
    private val track2CharacteristicUUID = UUID.fromString("00002222-0000-1000-8000-00805f9b34fb")
    private val controlPointCharacteristicUUID = UUID.fromString("00003333-0000-1000-8000-00805f9b34fb")

    // Characteristic references
    private var track1Characteristic: BluetoothGattCharacteristic? = null
    private var track2Characteristic: BluetoothGattCharacteristic? = null
    private var controlPointCharacteristic: BluetoothGattCharacteristic? = null

    // Write Queue for sequential operations
    private val writeQueue = ConcurrentLinkedQueue<BluetoothGattCharacteristic>()
    private var isWriting = false

    // Scan Callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val currentList = _discoveredDevices.value.toMutableList()
            // Add if not already present
            if (currentList.none { it.device.address == result.device.address }) {
                currentList.add(result)
                _discoveredDevices.value = currentList
            }
        }
    }

    // GATT Callback
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _connectionState.value = ConnectionState.CONNECTED
                        bluetoothGatt = gatt
                        writeQueue.clear()
                        isWriting = false
                        // Discover services upon connection
                        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                            bluetoothGatt?.discoverServices()
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _connectionState.value = ConnectionState.DISCONNECTED
                        bluetoothGatt?.close()
                        bluetoothGatt = null
                        writeQueue.clear()
                        isWriting = false
                    }
                }
            } else {
                Log.e("BleCommunicationService", "onConnectionStateChange received error status: $status")
                _connectionState.value = ConnectionState.DISCONNECTED
                bluetoothGatt?.close()
                bluetoothGatt = null
                writeQueue.clear()
                isWriting = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(magSpoofServiceUUID)
                track1Characteristic = service?.getCharacteristic(track1CharacteristicUUID)
                track2Characteristic = service?.getCharacteristic(track2CharacteristicUUID)
                controlPointCharacteristic = service?.getCharacteristic(controlPointCharacteristicUUID)
            } else {
                Log.e("BleCommunicationService", "onServicesDiscovered received error status: $status")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (writeQueue.isEmpty()) {
                    _transmissionStatus.value = "Transmission successful!"
                }
            } else {
                _transmissionStatus.value = "Transmission failed!"
                writeQueue.clear()
            }
            isWriting = false
            // Process next item in queue
            processWriteQueue()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        // Ensure service runs in foreground to prevent system killing it during scanning/connection
        startForegroundService()
    }

    /**
     * Starts scanning for BLE peripherals.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        _discoveredDevices.value = emptyList() // Clear previous results
        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
    }

    /**
     * Stops the BLE scan.
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    /**
     * Connects to a selected BLE device.
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        stopScan()
        _connectionState.value = ConnectionState.CONNECTING
        device.connectGatt(this, false, gattCallback)
    }

    /**
     * Disconnects from the current device.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        bluetoothGatt?.disconnect()
    }

    /**
     * Queues track data for transmission to the device.
     */
    @Suppress("DEPRECATION")
    fun writeTrackData(track1: String, track2: String) {
        _transmissionStatus.value = "Transmitting..."
        track1Characteristic?.let {
            it.value = track1.toByteArray()
            writeQueue.add(it)
        }
        track2Characteristic?.let {
            it.value = track2.toByteArray()
            writeQueue.add(it)
        }
        processWriteQueue()
    }

    /**
     * Sends the 'Transmit' command (trigger) to the device.
     */
    @Suppress("DEPRECATION")
    fun sendTransmitCommand() {
        _transmissionStatus.value = "Transmitting..."
        controlPointCharacteristic?.let {
            it.value = byteArrayOf(0x01)
            writeQueue.add(it)
        }
        processWriteQueue()
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun processWriteQueue() {
        if (isWriting || writeQueue.isEmpty()) {
            return
        }
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        isWriting = true
        bluetoothGatt?.writeCharacteristic(writeQueue.poll())
    }

    private fun startForegroundService() {
        val channelId = "ble_service_channel"
        val channelName = "BLE Communication Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MagNom")
            .setContentText("BLE Service is running.")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Use default or app icon
            .build()
        startForeground(1, notification)
    }

    private fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (permission == Manifest.permission.BLUETOOTH_SCAN || permission == Manifest.permission.BLUETOOTH_CONNECT)) {
            return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
            (permission == Manifest.permission.BLUETOOTH_ADMIN || permission == Manifest.permission.ACCESS_FINE_LOCATION)) {
            return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleCommunicationService = this@BleCommunicationService
    }
}
