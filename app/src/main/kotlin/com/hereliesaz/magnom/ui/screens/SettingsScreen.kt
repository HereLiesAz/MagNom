package com.hereliesaz.magnom.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.services.ConnectionState
import com.hereliesaz.magnom.viewmodels.SettingsViewModel
import android.net.Uri

@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = viewModel()) {
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val backupEnabled by viewModel.backupEnabled.collectAsState()
    val backupPassword by viewModel.backupPassword.collectAsState()
    val showRestorePasswordDialog by viewModel.showRestorePasswordDialog.collectAsState()
    val showRestartDialog by viewModel.showRestartDialog.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.startScan()
        }
    }

    val context = LocalContext.current
    var backupFilename by remember { mutableStateOf("backup.magnom") }
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/magnom")
    ) { uri ->
        uri?.let {
            viewModel.setBackupLocation(it)
        }
    }

    var restoreFileUri by remember { mutableStateOf<Uri?>(null) }
    val fileRestoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            restoreFileUri = it
            viewModel.onRestoreBackupClicked()
        }
    }

    if (showRestorePasswordDialog) {
        var restorePassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.onRestoreDialogDismissed() },
            title = { Text("Enter Backup Password") },
            text = {
                OutlinedTextField(
                    value = restorePassword,
                    onValueChange = { restorePassword = it },
                    label = { Text("Password") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        restoreFileUri?.let {
                            viewModel.onRestorePasswordEntered(restorePassword, it)
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onRestoreDialogDismissed() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onRestartDialogDismissed() },
            title = { Text("Restart Required") },
            text = { Text("The application must be restarted for the changes to take effect.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onRestartDialogDismissed()
                        // You can't programmatically restart an app.
                        // Instruct the user to do it manually.
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Bluetooth Section ---
        Text("Bluetooth", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            }) {
                Text("Start Scan")
            }
            Button(onClick = { viewModel.stopScan() }) {
                Text("Stop Scan")
            }
        }
        Text("Status: ${connectionState.name}")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(discoveredDevices) { device ->
                Text(
                    text = "${device.device.name ?: "Unknown"} (${device.device.address})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.connect(device.device) }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Backup Section ---
        Text("Backup & Restore", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Real-time Backup", modifier = Modifier.weight(1f))
            Switch(
                checked = backupEnabled,
                onCheckedChange = { viewModel.setBackupEnabled(it) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = backupPassword,
            onValueChange = { viewModel.setBackupPassword(it) },
            label = { Text("Backup Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = backupEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = backupFilename,
            onValueChange = { backupFilename = it },
            label = { Text("Backup Filename") },
            modifier = Modifier.fillMaxWidth(),
            enabled = backupEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { filePickerLauncher.launch(backupFilename) },
            modifier = Modifier.fillMaxWidth(),
            enabled = backupEnabled
        ) {
            Text("Set Backup Location")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { fileRestoreLauncher.launch(arrayOf("application/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore from Backup")
        }
    }
}
