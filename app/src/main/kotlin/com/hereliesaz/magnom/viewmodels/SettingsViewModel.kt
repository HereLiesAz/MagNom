package com.hereliesaz.magnom.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.SettingsRepository
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.services.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private var bleCommunicationService: BleCommunicationService? = null

    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _backupPassword = MutableStateFlow("")
    val backupPassword: StateFlow<String> = _backupPassword.asStateFlow()

    private val _backupUri = MutableStateFlow<Uri?>(null)
    val backupUri: StateFlow<Uri?> = _backupUri.asStateFlow()

    fun setBleCommunicationService(service: BleCommunicationService) {
        bleCommunicationService = service
        viewModelScope.launch {
            service.discoveredDevices.collect { _discoveredDevices.value = it }
        }
        viewModelScope.launch {
            service.connectionState.collect { _connectionState.value = it }
        }
    }

    fun setBackupPassword(password: String) {
        _backupPassword.value = password
    }

    fun setBackupUri(uri: Uri?) {
        _backupUri.value = uri
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

    fun backup() {
        viewModelScope.launch {
            val password = backupPassword.value
            val uri = backupUri.value
            if (password.isNotEmpty() && uri != null) {
                backupManager.createBackup(password, uri.toString())
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            val password = backupPassword.value
            val uri = backupUri.value
            if (password.isNotEmpty() && uri != null) {
                backupManager.restoreBackup(password, uri)
            }
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, backupManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
