package com.hereliesaz.magnom.viewmodels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.data.Device
import com.hereliesaz.magnom.data.DeviceRepository
import com.hereliesaz.magnom.data.DeviceType
import com.hereliesaz.magnom.services.UsbCommunicationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeviceScreenUiState(
    val devices: List<Device> = emptyList(),
    val expandedDeviceIds: Set<String> = emptySet(),
)

class DeviceViewModel : ViewModel() {

    private val deviceRepository = DeviceRepository()

    private val _uiState = MutableStateFlow(DeviceScreenUiState())
    val uiState: StateFlow<DeviceScreenUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        _uiState.update { it.copy(devices = deviceRepository.getDevices()) }
    }

    fun onDeviceClicked(deviceId: String) {
        _uiState.update { currentState ->
            val expandedIds = currentState.expandedDeviceIds.toMutableSet()
            if (expandedIds.contains(deviceId)) {
                expandedIds.remove(deviceId)
            } else {
                expandedIds.add(deviceId)
            }
            currentState.copy(expandedDeviceIds = expandedIds)
        }
    }

    fun onPinDeviceClicked(device: Device) {
        val updatedDevices = _uiState.value.devices.toMutableList()
        val deviceIndex = updatedDevices.indexOf(device)
        if (deviceIndex != -1) {
            val updatedDevice = device.copy(isPinned = !device.isPinned)
            updatedDevices[deviceIndex] = updatedDevice
            _uiState.update {
                it.copy(devices = updatedDevices.sortedByDescending { d -> d.isPinned })
            }
        }
    }

    private var usbService: UsbCommunicationService? = null
    private var isUsbServiceBound = false

    private val usbServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as UsbCommunicationService.UsbBinder
            usbService = binder.getService()
            isUsbServiceBound = true
            usbService?.connect()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            usbService = null
            isUsbServiceBound = false
        }
    }

    fun onEnableDeviceClicked(context: Context, device: Device) {
        if (device.type == DeviceType.USB_SERIAL) {
            val intent = Intent(context, UsbCommunicationService::class.java)
            context.bindService(intent, usbServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isUsbServiceBound) {
            // It's important to unbind the service when the ViewModel is destroyed
            // to avoid leaking the service connection.
            // However, the context is not available here. This needs to be handled in the UI layer.
        }
    }
}
