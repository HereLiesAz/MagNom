package com.hereliesaz.magnom.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.IBinder
import com.hereliesaz.magnom.utils.Result
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class UsbCommunicationService : Service() {

    private val binder = UsbBinder()
    private var serialPort: UsbSerialPort? = null
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    inner class UsbBinder : Binder() {
        fun getService(): UsbCommunicationService = this@UsbCommunicationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun getAvailableDevices(): List<UsbDevice> {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager).map { it.device }
    }

    fun connect(device: UsbDevice, permissionIntent: PendingIntent): Result<Unit> {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (!manager.hasPermission(device)) {
            manager.requestPermission(device, permissionIntent)
            return Result.Error("Permission required")
        }

        _connectionState.value = ConnectionState.CONNECTING
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("No driver found for device")
        }

        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("Failed to open device")
        }

        serialPort = driver.ports[0]
        try {
            serialPort?.open(connection)
            serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            _connectionState.value = ConnectionState.CONNECTED
            return Result.Success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("Failed to connect: ${e.message}")
        }
    }

    fun sendCommand(command: String): Result<Unit> {
        return if (serialPort != null && serialPort!!.isOpen) {
            try {
                serialPort?.write((command + "\n").toByteArray(), 2000)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Failed to send command: ${e.message}")
            }
        } else {
            Result.Error("Device not connected")
        }
    }

    fun disconnect() {
        if (serialPort != null && serialPort!!.isOpen) {
            serialPort?.close()
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
