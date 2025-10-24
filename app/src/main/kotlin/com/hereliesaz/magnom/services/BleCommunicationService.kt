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
import com.hereliesaz.magnom.logic.TrackDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

class BleCommunicationService : Service() {

    private val binder = LocalBinder()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val writeQueue = ConcurrentLinkedQueue<BluetoothGattCharacteristic>()
    private var isWriting = false


    // UUIDs from documentation
    private val magSpoofServiceUUID: UUID = UUID.fromString("0000BEEF-1212-EFEF-1523-785FEABCD123")
    private val track1DataUUID: UUID = UUID.fromString("0000B0B1-1212-EFEF-1523-785FEABCD123")
    private val track2DataUUID: UUID = UUID.fromString("0000B0B2-1212-EFEF-1523-785FEABCD123")
    private val controlPointUUID: UUID = UUID.fromString("0000B0B3-1212-EFEF-1523-785FEABCD123")

    private var track1Characteristic: BluetoothGattCharacteristic? = null
    private var track2Characteristic: BluetoothGattCharacteristic? = null
    private var controlPointCharacteristic: BluetoothGattCharacteristic? = null


    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _transmissionStatus = MutableStateFlow("")
    val transmissionStatus: StateFlow<String> = _transmissionStatus.asStateFlow()


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (_discoveredDevices.value.none { it.device.address == result.device.address }) {
                _discoveredDevices.value = _discoveredDevices.value + result
            }
        }
    }

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
                track1Characteristic = service?.getCharacteristic(track1DataUUID)
                track2Characteristic = service?.getCharacteristic(track2DataUUID)
                controlPointCharacteristic = service?.getCharacteristic(controlPointUUID)
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
            processWriteQueue()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        startForegroundService()
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        _discoveredDevices.value = emptyList() // Clear previous results
        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        stopScan()
        _connectionState.value = ConnectionState.CONNECTING
        device.connectGatt(this, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        bluetoothGatt?.disconnect()
    }

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
