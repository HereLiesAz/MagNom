package com.hereliesaz.magnom.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.usb.UsbDevice
import android.os.IBinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hereliesaz.magnom.services.ConnectionState
import com.hereliesaz.magnom.services.UsbCommunicationService
import com.hereliesaz.magnom.viewmodels.DeviceViewModel

@Composable
fun DeviceScreen(deviceViewModel: DeviceViewModel = viewModel()) {
    val uiState by deviceViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val usbServiceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as UsbCommunicationService.UsbBinder
                deviceViewModel.onServiceConnected(binder.getService())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                deviceViewModel.onServiceDisconnected()
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, UsbCommunicationService::class.java)
        context.bindService(intent, usbServiceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(usbServiceConnection)
        }
    }

    if (uiState.usbDevices.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No USB devices found.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { deviceViewModel.refreshUsbDevices() }) {
                    Text("Refresh USB Devices")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Button(onClick = { deviceViewModel.refreshUsbDevices() }) {
                    Text("Refresh USB Devices")
                }
            }
            items(uiState.usbDevices) { device ->
                UsbDeviceListItem(
                    device = device,
                    connectionState = if (uiState.connectedDevice?.deviceId == device.deviceId) uiState.connectionState else ConnectionState.DISCONNECTED,
                    onConnectClicked = { deviceViewModel.connectToDevice(device) },
                    onSendCommand = { track1, track2 -> deviceViewModel.sendSpoofCommands(track1, track2) }
                )
            }
        }
    }
}

@Composable
fun UsbDeviceListItem(
    device: UsbDevice,
    connectionState: ConnectionState,
    onConnectClicked: () -> Unit,
    onSendCommand: (String, String) -> Unit
) {
    var track1 by remember { mutableStateOf("") }
    var track2 by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.deviceName, style = MaterialTheme.typography.titleLarge)
            Text(text = "Vendor ID: ${device.vendorId}, Product ID: ${device.productId}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: $connectionState")
            Spacer(modifier = Modifier.height(8.dp))

            if (connectionState == ConnectionState.CONNECTED) {
                OutlinedTextField(
                    value = track1,
                    onValueChange = { track1 = it },
                    label = { Text("Track 1 Data") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = track2,
                    onValueChange = { track2 = it },
                    label = { Text("Track 2 Data") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onSendCommand(track1, track2) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send and Spoof")
                }
            } else {
                Button(onClick = onConnectClicked) {
                    Text(if (connectionState == ConnectionState.CONNECTING) "Connecting..." else "Connect")
                }
            }
        }
    }
}
