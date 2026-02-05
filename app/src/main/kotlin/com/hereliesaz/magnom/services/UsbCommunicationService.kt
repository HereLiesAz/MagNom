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

/**
 * A Service for managing communication with USB-connected magnetic stripe emulation devices.
 *
 * This service handles:
 * - Enumerating available USB serial devices.
 * - Requesting USB permissions.
 * - Establishing a serial connection (CDC-ACM, FTDI, etc.).
 * - Sending commands and data to the device.
 *
 * It runs as a bound service, allowing ViewModels to bind to it and call methods directly.
 */
class UsbCommunicationService : Service() {

    private val binder = UsbBinder()
    private var serialPort: UsbSerialPort? = null
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)

    /**
     * Observable flow of the current connection state.
     */
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    inner class UsbBinder : Binder() {
        fun getService(): UsbCommunicationService = this@UsbCommunicationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Lists all connected USB devices that appear to be serial converters.
     *
     * @return A list of [UsbDevice] objects.
     */
    fun getAvailableDevices(): List<UsbDevice> {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager).map { it.device }
    }

    /**
     * Connects to a specific USB device.
     *
     * @param device The USB device to connect to.
     * @param permissionIntent A PendingIntent to handle the permission result (if permission is not yet granted).
     * @return [Result.Success] if connected, or [Result.Error] with details.
     */
    fun connect(device: UsbDevice, permissionIntent: PendingIntent): Result<Unit> {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Request permission if not granted
        if (!manager.hasPermission(device)) {
            manager.requestPermission(device, permissionIntent)
            return Result.Error("Permission required")
        }

        _connectionState.value = ConnectionState.CONNECTING

        // Find the appropriate driver for the device
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("No driver found for device")
        }

        // Open the USB connection
        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("Failed to open device")
        }

        // Initialize the serial port (typically port 0)
        serialPort = driver.ports[0]
        try {
            serialPort?.open(connection)
            // Configure serial parameters: 115200 baud, 8N1
            serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            _connectionState.value = ConnectionState.CONNECTED
            return Result.Success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return Result.Error("Failed to connect: ${e.message}")
        }
    }

    /**
     * Sends a command string to the connected device.
     *
     * Appends a newline character to the command.
     *
     * @param command The command string to send.
     * @return [Result.Success] if sent, or [Result.Error] if failed or disconnected.
     */
    fun sendCommand(command: String): Result<Unit> {
        return if (serialPort != null && serialPort!!.isOpen) {
            try {
                // Write command followed by newline, with a 2-second timeout
                serialPort?.write((command + "\n").toByteArray(), 2000)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Failed to send command: ${e.message}")
            }
        } else {
            Result.Error("Device not connected")
        }
    }

    /**
     * Closes the connection and releases resources.
     */
    fun disconnect() {
        if (serialPort != null && serialPort!!.isOpen) {
            serialPort?.close()
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
