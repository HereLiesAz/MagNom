package com.hereliesaz.magnom.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.*
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.services.UsbCommunicationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * ViewModel for the Transmission Interface screen.
 *
 * Coordinates data transmission between the repository and the communication services (BLE/USB).
 */
class TransmissionInterfaceViewModel(
    application: Application,
    private val deviceRepository: DeviceRepository,
    private val usbCommunicationService: UsbCommunicationService,
    private val cardId: String
) : AndroidViewModel(application) {

    // Instantiate BackupManager and pass it to CardRepository
    private val cardRepository = CardRepository(application, BackupManager(application))
    private var bleServiceCollectionJob: Job? = null
    private val trackDataGenerator = TrackDataGenerator()

    private var bleService: BleCommunicationService? = null
    private var usbService: UsbCommunicationService? = null
    private val _cardProfile = MutableStateFlow<CardProfile?>(null)
    val cardProfile: StateFlow<CardProfile?> = _cardProfile.asStateFlow()
    private val _transmissionStatus = MutableStateFlow("")
    val transmissionStatus: StateFlow<String> = _transmissionStatus.asStateFlow()

    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleCommunicationService.LocalBinder
            bleService = binder.getService()
            bleServiceCollectionJob = viewModelScope.launch {
                bleService?.transmissionStatus?.collect { status ->
                    _transmissionStatus.value = status
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            bleServiceCollectionJob?.cancel()
        }
    }

    private val usbServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as UsbCommunicationService.UsbBinder
            usbService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            usbService = null
        }
    }

    init {
        viewModelScope.launch {
            _cardProfile.value = cardRepository.getCardProfile(cardId)
        }
        Intent(application, BleCommunicationService::class.java).also { intent ->
            application.bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(application, UsbCommunicationService::class.java).also { intent ->
            application.bindService(intent, usbServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun transmit() {
        _cardProfile.value?.let { profile ->
            val track1 = trackDataGenerator.generateTrack1(profile.pan, profile.name, profile.expirationDate, profile.serviceCode)
            val track2 = trackDataGenerator.generateTrack2(profile.pan, profile.expirationDate, profile.serviceCode)

            val activeDevice = deviceRepository.getDevices().firstOrNull { it.isPinned }
            if (activeDevice?.type == DeviceType.USB_SERIAL) {
                usbService?.sendCommand("T1:$track1")
                usbService?.sendCommand("T2:$track2")
                usbService?.sendCommand("SPOOF")
                _transmissionStatus.value = "Transmission successful!"
            } else {
                bleService?.writeTrackData(track1, track2)
                bleService?.sendTransmitCommand()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(bleServiceConnection)
        getApplication<Application>().unbindService(usbServiceConnection)
    }
}
