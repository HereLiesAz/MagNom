package magnom.viewmodels

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

class TransmissionInterfaceViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application)
    private var serviceCollectionJob: Job? = null
    private val trackDataGenerator = TrackDataGenerator()
    private val cardId: String = savedStateHandle.get<String>("cardId")!!

    private var bleService: BleCommunicationService? = null
    private val _cardProfile = MutableStateFlow<CardProfile?>(null)
    val cardProfile: StateFlow<CardProfile?> = _cardProfile.asStateFlow()
    private val _transmissionStatus = MutableStateFlow("")
    val transmissionStatus: StateFlow<String> = _transmissionStatus.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleCommunicationService.LocalBinder
            bleService = binder.getService()
            serviceCollectionJob = viewModelScope.launch {
                bleService?.transmissionStatus?.collect { status ->
                    _transmissionStatus.value = status
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            serviceCollectionJob?.cancel()
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
            val track1 = trackDataGenerator.generateTrack1(profile.pan, profile.name, profile.expirationDate, profile.serviceCode)
            val track2 = trackDataGenerator.generateTrack2(profile.pan, profile.expirationDate, profile.serviceCode)
            bleService?.writeTrackData(track1, track2)
            bleService?.sendTransmitCommand()
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
