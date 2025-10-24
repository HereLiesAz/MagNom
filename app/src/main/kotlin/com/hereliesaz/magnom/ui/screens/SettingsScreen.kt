package com.hereliesaz.magnom.ui.screens

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.SettingsRepository
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.viewmodels.SettingsViewModel
import com.hereliesaz.magnom.viewmodels.SettingsViewModelFactory

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context: Context = LocalContext.current.applicationContext
    val settingsRepository = SettingsRepository(context)
    val backupManager = BackupManager(context)
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsRepository, backupManager)
    )

    var bleService by remember { mutableStateOf<BleCommunicationService?>(null) }
    val serviceConnection = remember { object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleCommunicationService.LocalBinder
            bleService = binder.getService()
            viewModel.setBleCommunicationService(binder.getService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
        }
    } }

    DisposableEffect(Unit) {
        Intent(context, BleCommunicationService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val backupPassword by viewModel.backupPassword.collectAsState()
    val backupUri by viewModel.backupUri.collectAsState()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        viewModel.setBackupUri(uri)
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.setBackupUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row {
                Button(onClick = { viewModel.startScan() }, enabled = bleService != null) {
                    Text("Start Scan")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.stopScan() }, enabled = bleService != null) {
                    Text("Stop Scan")
                }
            }
            Text("Connection State: $connectionState")
            LazyColumn {
                items(discoveredDevices) { device ->
                    Text(
                        text = "${device.device.name} - ${device.device.address}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.connect(device.device) }
                            .padding(8.dp)
                    )
                }
            }
            OutlinedTextField(
                value = backupPassword,
                onValueChange = { viewModel.setBackupPassword(it) },
                label = { Text("Backup Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = { backupLauncher.launch("magnom_backup.zip") }) {
                    Text("Select Backup Location")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { restoreLauncher.launch(arrayOf("application/zip")) }) {
                    Text("Select Backup to Restore")
                }
            }
            Text("Selected backup file: ${backupUri?.path}")
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = { viewModel.backup() }) {
                    Text("Backup")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.restore() }) {
                    Text("Restore")
                }
            }
        }
    }
}
