package com.hereliesaz.magnom.viewmodels

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.services.ConnectionState
import com.hereliesaz.magnom.services.UsbCommunicationService
import com.hereliesaz.magnom.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceUiState(
    val usbDevices: List<UsbDevice> = emptyList(),
    val connectedDevice: UsbDevice? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val errorMessage: String? = null
)

class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private var usbService: UsbCommunicationService? = null
    private val ACTION_USB_PERMISSION = "com.hereliesaz.magnom.USB_PERMISSION"

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    @Suppress("DEPRECATION") val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { connectToDevice(it) }
                    } else {
                        _uiState.update { it.copy(errorMessage = "Permission denied for device") }
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            getApplication(),
            usbPermissionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(usbPermissionReceiver)
    }

    fun onServiceConnected(service: UsbCommunicationService) {
        usbService = service
        refreshUsbDevices()
        viewModelScope.launch {
            usbService?.connectionState?.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    fun onServiceDisconnected() {
        usbService = null
    }

    fun refreshUsbDevices() {
        _uiState.update { it.copy(usbDevices = usbService?.getAvailableDevices() ?: emptyList()) }
    }

    fun connectToDevice(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        when (val result = usbService?.connect(device, permissionIntent)) {
            is Result.Success -> {
                _uiState.update { it.copy(connectedDevice = device, errorMessage = null) }
            }
            is Result.Error -> {
                if (result.message != "Permission required") {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            null -> {
                _uiState.update { it.copy(errorMessage = "USB service not available") }
            }
        }
    }

    fun sendSpoofCommands(track1: String, track2: String) {
        val track1Formatted = track1.replace('&', '^').replace('-', '/')
        val track2Formatted = track2.replace('ñ', ';').replace('¿', '=')

        if (track1Formatted.isNotEmpty()) {
            usbService?.sendCommand("T1:$track1Formatted")
        }
        if (track2Formatted.isNotEmpty()) {
            usbService?.sendCommand("T2:$track2Formatted")
        }
        usbService?.sendCommand("SPOOF")
    }
}
