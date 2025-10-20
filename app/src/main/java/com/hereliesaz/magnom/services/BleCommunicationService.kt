package com.hereliesaz.magnom.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleCommunicationService : Service() {

    private val binder = LocalBinder()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (_discoveredDevices.value.none { it.device.address == result.device.address }) {
                _discoveredDevices.value = _discoveredDevices.value + result
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        startForegroundService()
    }

    fun startScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            _discoveredDevices.value = emptyList() // Clear previous results
            bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        }
    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
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

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleCommunicationService = this@BleCommunicationService
    }
}
