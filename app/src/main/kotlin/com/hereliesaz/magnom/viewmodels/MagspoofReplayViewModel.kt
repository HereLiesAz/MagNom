package com.hereliesaz.magnom.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.services.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MagspoofReplayViewModel(
    private val cardRepository: CardRepository
) : ViewModel() {

    private var bleCommunicationService: BleCommunicationService? = null

    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _transmissionStatus = MutableStateFlow("")
    val transmissionStatus: StateFlow<String> = _transmissionStatus.asStateFlow()

    private val _selectedCard = MutableStateFlow<CardProfile?>(null)
    val selectedCard: StateFlow<CardProfile?> = _selectedCard.asStateFlow()

    fun setBleCommunicationService(service: BleCommunicationService) {
        bleCommunicationService = service
        viewModelScope.launch {
            service.discoveredDevices.collect { _discoveredDevices.value = it }
        }
        viewModelScope.launch {
            service.connectionState.collect { _connectionState.value = it }
        }
        viewModelScope.launch {
            service.transmissionStatus.collect { _transmissionStatus.value = it }
        }
    }

    fun setSelectedCard(cardId: String?) {
        viewModelScope.launch {
            if (cardId != null) {
                _selectedCard.value = cardRepository.getCardProfile(cardId)
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            bleCommunicationService?.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            bleCommunicationService?.stopScan()
        }
    }

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            bleCommunicationService?.connect(device)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bleCommunicationService?.disconnect()
        }
    }

    fun writeTrackData(track1: String, track2: String) {
        viewModelScope.launch {
            bleCommunicationService?.writeTrackData(track1, track2)
        }
    }

    fun sendTransmitCommand() {
        viewModelScope.launch {
            bleCommunicationService?.sendTransmitCommand()
        }
    }
}

class MagspoofReplayViewModelFactory(
    private val cardRepository: CardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MagspoofReplayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MagspoofReplayViewModel(cardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
