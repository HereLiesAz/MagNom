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
import coil.compose.AsyncImage
import com.hereliesaz.magnom.data.Device
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

    if (uiState.devices.isEmpty() && uiState.usbDevices.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No devices found.")
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
            items(uiState.devices) { device ->
                DeviceListItem(
                    device = device,
                    isExpanded = uiState.expandedDeviceIds.contains(device.id),
                    onDeviceClicked = { deviceViewModel.onDeviceClicked(device.id) },
                    onPinDeviceClicked = { deviceViewModel.onPinDeviceClicked(device) },
                    onEnableDeviceClicked = {
                        // This is handled by the UsbDeviceListItem
                    }
                )
            }
            items(uiState.usbDevices) { device ->
                UsbDeviceListItem(
                    device = device,
                    onConnectClicked = { deviceViewModel.onEnableDeviceClicked(device) }
                )
            }
        }
    }
}

@Composable
fun UsbDeviceListItem(
    device: UsbDevice,
    onConnectClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.deviceName)
            Button(onClick = onConnectClicked) {
                Text("Connect")
            }
        }
    }
}

@Composable
fun DeviceListItem(
    device: Device,
    isExpanded: Boolean,
    onDeviceClicked: () -> Unit,
    onPinDeviceClicked: () -> Unit,
    onEnableDeviceClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onDeviceClicked),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name, style = MaterialTheme.typography.titleLarge)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = device.imageUrl,
                        contentDescription = device.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = device.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    device.links.forEach { (title, url) ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { /* Handle link click */ }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = onPinDeviceClicked) {
                            Text(if (device.isPinned) "Unpin" else "Pin")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onEnableDeviceClicked) {
                            Text("Enable Device")
                        }
                    }
                }
            }
        }
    }
}
