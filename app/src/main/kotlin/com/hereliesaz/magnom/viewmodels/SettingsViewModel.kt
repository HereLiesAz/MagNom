package com.hereliesaz.magnom.viewmodels

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
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
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.SettingsRepository
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.services.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import android.net.Uri

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private var bleService: BleCommunicationService? = null
    private val backupManager = BackupManager(application)
    private val settingsRepository = SettingsRepository(application)

    // UI State
    private val _discoveredDevices = MutableStateFlow<List<ScanResult>>(emptyList())
    val discoveredDevices: StateFlow<List<ScanResult>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _backupEnabled = MutableStateFlow(false)
    val backupEnabled: StateFlow<Boolean> = _backupEnabled.asStateFlow()

    private val _backupPassword = MutableStateFlow("")
    val backupPassword: StateFlow<String> = _backupPassword.asStateFlow()

    private val _backupLocation = MutableStateFlow<String?>(null)
    val backupLocation: StateFlow<String?> = _backupLocation.asStateFlow()

    private val _showRestorePasswordDialog = MutableStateFlow(false)
    val showRestorePasswordDialog: StateFlow<Boolean> = _showRestorePasswordDialog.asStateFlow()

    private val _showRestartDialog = MutableStateFlow(false)
    val showRestartDialog: StateFlow<Boolean> = _showRestartDialog.asStateFlow()


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleService = (service as BleCommunicationService.LocalBinder).getService()
            viewModelScope.launch {
                bleService?.discoveredDevices?.collect {
                    _discoveredDevices.value = it
                }
            }
            viewModelScope.launch {
                bleService?.connectionState?.collect {
                    _connectionState.value = it
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
        _backupEnabled.value = settingsRepository.isBackupEnabled()
        _backupPassword.value = settingsRepository.getBackupPassword()
        _backupLocation.value = settingsRepository.getBackupLocation().takeIf { it.isNotEmpty() }
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

    fun connect(device: BluetoothDevice) {
        bleService?.connect(device)
    }

    fun stopScan() {
        bleService?.stopScan()
    }

    // --- Backup and Restore ---

    fun setBackupEnabled(enabled: Boolean) {
        _backupEnabled.value = enabled
        settingsRepository.setBackupEnabled(enabled)
        backupNow()
    }

    fun setBackupPassword(password: String) {
        _backupPassword.value = password
        settingsRepository.setBackupPassword(password)
        backupNow()
    }

    fun setBackupLocation(uri: Uri) {
        _backupLocation.value = uri.toString()
        settingsRepository.setBackupLocation(uri.toString())
        backupNow()
    }

    private fun backupNow() {
        if (_backupEnabled.value) {
            val password = _backupPassword.value
            val location = _backupLocation.value
            if (password.isNotEmpty() && location != null) {
                backupManager.createBackup(password, location)
            }
        }
    }

    fun onRestoreBackupClicked() {
        _showRestorePasswordDialog.value = true
    }

    fun onRestorePasswordEntered(password: String, sourceUri: Uri) {
        _showRestorePasswordDialog.value = false
        if (password.isNotEmpty()) {
            val success = backupManager.restoreBackup(password, sourceUri)
            if (success) {
                _showRestartDialog.value = true
            }
        }
    }

    fun onRestoreDialogDismissed() {
        _showRestorePasswordDialog.value = false
    }

    fun onRestartDialogDismissed() {
        _showRestartDialog.value = false
    }


    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
