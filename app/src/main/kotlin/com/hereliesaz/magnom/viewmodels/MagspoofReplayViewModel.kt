package com.hereliesaz.magnom.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.services.ConnectionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MagspoofReplayViewModel(
    private val bleCommunicationService: BleCommunicationService
) : ViewModel() {

    val discoveredDevices: StateFlow<List<ScanResult>> = bleCommunicationService.discoveredDevices
    val connectionState: StateFlow<ConnectionState> = bleCommunicationService.connectionState
    val transmissionStatus: StateFlow<String> = bleCommunicationService.transmissionStatus

    fun startScan() {
        viewModelScope.launch {
            bleCommunicationService.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            bleCommunicationService.stopScan()
        }
    }

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            bleCommunicationService.connect(device)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bleCommunicationService.disconnect()
        }
    }

    fun writeTrackData(track1: String, track2: String) {
        viewModelScope.launch {
            bleCommunicationService.writeTrackData(track1, track2)
        }
    }

    fun sendTransmitCommand() {
        viewModelScope.launch {
            bleCommunicationService.sendTransmitCommand()
        }
    }
}

class MagspoofReplayViewModelFactory(
    private val bleCommunicationService: BleCommunicationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MagspoofReplayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MagspoofReplayViewModel(bleCommunicationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
