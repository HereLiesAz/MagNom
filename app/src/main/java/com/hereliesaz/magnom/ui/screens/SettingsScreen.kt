package com.hereliesaz.magnom.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = viewModel()) {
    val discoveredDevices by viewModel.discoveredDevices?.collectAsState(initial = emptyList()) ?: return

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startScan()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION))
        }) {
            Text("Start Scan")
        }
        Button(onClick = { viewModel.stopScan() }) {
            Text("Stop Scan")
        }
        LazyColumn {
            items(discoveredDevices) { device ->
                Text(device.device.address)
            }
        }
    }
}
