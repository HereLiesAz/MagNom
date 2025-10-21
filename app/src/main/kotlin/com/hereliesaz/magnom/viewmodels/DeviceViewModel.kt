package com.hereliesaz.magnom.viewmodels

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.data.Device
import com.hereliesaz.magnom.data.DeviceRepository
import com.hereliesaz.magnom.services.UsbCommunicationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeviceScreenUiState(
    val devices: List<Device> = emptyList(),
    val expandedDeviceIds: Set<String> = emptySet(),
    val usbDevices: List<UsbDevice> = emptyList()
)

class DeviceViewModel : ViewModel() {

    private val deviceRepository = DeviceRepository()
    private var usbService: UsbCommunicationService? = null

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

    fun onServiceConnected(service: UsbCommunicationService) {
        usbService = service
        refreshUsbDevices()
    }

    fun onServiceDisconnected() {
        usbService = null
        _uiState.update { it.copy(usbDevices = emptyList()) }
    }

    fun refreshUsbDevices() {
        usbService?.let {
            _uiState.update { currentState ->
                currentState.copy(usbDevices = it.getAvailableDevices())
            }
        }
    }

    fun onEnableDeviceClicked(device: UsbDevice) {
        usbService?.connect(device)
    }
}
