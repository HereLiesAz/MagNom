package com.hereliesaz.magnom.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.AudioFileViewModel
import com.hereliesaz.magnom.viewmodels.AudioRecordingViewModel
import java.io.File
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordingScreen(
    navController: NavController,
    audioFileViewModel: AudioFileViewModel
) {
    val context = LocalContext.current
    val viewModel: AudioRecordingViewModel = viewModel()
    var isRecording by remember { mutableStateOf(false) }
    val audioData by viewModel.audioData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val savedFilePath by viewModel.savedFilePath.collectAsState()
    val availableDevices by viewModel.availableDevices.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getAvailableRecordingDevices(context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
    )

    Column {
        errorMessage?.let {
            Text(text = it)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val step = width / audioData.size

            for (i in 0 until audioData.size - 1) {
                val x1 = i * step
                val y1 = centerY + audioData[i] / 32768f * centerY
                val x2 = (i + 1) * step
                val y2 = centerY + audioData[i + 1] / 32768f * centerY
                drawLine(
                    color = Color.Red,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2f
                )
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedDevice?.productName ?: "Select a device",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                availableDevices.forEach { device ->
                    DropdownMenuItem(
                        text = { Text(device.productName) },
                        onClick = {
                            viewModel.onDeviceSelected(device)
                            expanded = false
                        }
                    )
                }
            }
        }
        Button(onClick = {
            when (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )) {
                PackageManager.PERMISSION_GRANTED -> {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording(context, selectedDevice)
                    }
                    isRecording = !isRecording
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }
        savedFilePath?.let {
            Text("Recording saved to: $it")
            Button(onClick = {
                audioFileViewModel.onFileSelected(context, Uri.fromFile(File(it)))
                navController.navigate(Screen.SwipeSelection.route)
            }) {
                Text("Parse Recording")
            }
        }
    }
}
