package com.hereliesaz.magnom.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.IBinder
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class UsbCommunicationService : Service() {

    private val binder = UsbBinder()
    private var serialPort: UsbSerialPort? = null

    inner class UsbBinder : Binder() {
        fun getService(): UsbCommunicationService = this@UsbCommunicationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun connect(): Boolean {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return false
        }

        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device) ?: return false

        serialPort = driver.ports[0]
        serialPort?.open(connection)
        serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        return true
    }

    fun sendCommand(command: String) {
        serialPort?.write((command + "\n").toByteArray(), 2000)
    }

    fun disconnect() {
        serialPort?.close()
    }
}
