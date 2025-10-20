package com.hereliesaz.magnom.viewmodels

import android.Manifest
import android.app.Application
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.services.BleCommunicationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private var bleService: BleCommunicationService? = null
    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleService = (service as BleCommunicationService.LocalBinder).getService()
            viewModelScope.launch {
                bleService?.discoveredDevices?.collect {
                    _discoveredDevices.value = it
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
        }
    }

    init {
        Intent(application, BleCommunicationService::class.java).also { intent ->
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bleService?.startScan()
        }
    }

    fun stopScan() {
        bleService?.stopScan()
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
