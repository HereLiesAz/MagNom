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
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.logic.TrackDataGenerator
import com.hereliesaz.magnom.services.BleCommunicationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransmissionInterfaceViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application)
    private val trackDataGenerator = TrackDataGenerator()
    private val cardId: String = requireNotNull(savedStateHandle.get<String>("cardId")) { "cardId argument is required." }

    private var bleService: BleCommunicationService? = null
    private val _cardProfile = MutableStateFlow<CardProfile?>(null)
    val cardProfile: StateFlow<CardProfile?> = _cardProfile.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleService = (service as BleCommunicationService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
        }
    }

    init {
        viewModelScope.launch {
            _cardProfile.value = cardRepository.getCardProfile(cardId)
        }
        Intent(application, BleCommunicationService::class.java).also { intent ->
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun transmit() {
        _cardProfile.value?.let { profile ->
            // For now, we'll just generate Track 2 data as Track 1 is not fully supported
            val track2 = trackDataGenerator.generateTrack2(profile.pan, profile.expirationDate, profile.serviceCode)
            bleService?.writeTrackData("", track2)
            bleService?.sendTransmitCommand()
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
