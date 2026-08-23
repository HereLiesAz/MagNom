package com.hereliesaz.magnom.transport

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
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

/**
 * Transmits a card to a wired MagSpoof-class device over USB serial. Sends a simple
 * MagSpoof line protocol (T1:/T2:/SPOOF). Only whole [Card]s are accepted,
 * so the serial device never receives empty track data.
 */
class UsbTransmitter(private val context: Context) : Transmitter {

    override val kind = TransportKind.USB

    private val _status = MutableStateFlow(TransportStatus.READY)
    override val status: StateFlow<TransportStatus> = _status.asStateFlow()

    private val _target = MutableStateFlow<String?>(null)
    override val target: StateFlow<String?> = _target.asStateFlow()

    private val usbManager get() = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    override fun isAvailable(): Boolean {
        val manager = usbManager ?: return false
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager).isNotEmpty()
    }

    override suspend fun transmit(card: Card): TransmitResult = withContext(Dispatchers.IO) {
        val manager = usbManager ?: return@withContext TransmitResult.Failure("USB unavailable")
        val driver = UsbSerialProber.getDefaultProber().findAllDrivers(manager).firstOrNull()
            ?: return@withContext TransmitResult.Failure("No USB serial device connected")
        val connection = manager.openDevice(driver.device)
            ?: return@withContext TransmitResult.Failure("USB permission not granted")

        val port: UsbSerialPort = driver.ports.first()
        try {
            _status.value = TransportStatus.CONNECTING
            port.open(connection)
            port.setParameters(MagSpoofProtocol.BAUD, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            _target.value = driver.device.deviceName
            _status.value = TransportStatus.TRANSMITTING
            for (frame in MagSpoofProtocol.commands(card)) port.write(frame, WRITE_TIMEOUT)
            TransmitResult.Success
        } catch (e: Throwable) {
            TransmitResult.Failure(e.message ?: "USB write failed")
        } finally {
            runCatching { port.close() }
            _status.value = TransportStatus.READY
        }
    }

    override fun release() { _status.value = TransportStatus.READY }

    companion object {
        private const val WRITE_TIMEOUT = 2_000
    }
}
