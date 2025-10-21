package com.hereliesaz.magnom.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.ParseViewModel
import com.hereliesaz.magnom.viewmodels.AudioRecordingViewModel
import com.hereliesaz.magnom.viewmodels.WaveformViewModel
import com.hereliesaz.magnom.viewmodels.WaveformViewModelFactory
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParseScreen(
    navController: NavController,
    cardId: String?,
    audioFileViewModel: AudioFileViewModel,
) {
    if (cardId != null) {
        val context = LocalContext.current
        val waveformViewModel: WaveformViewModel = viewModel(
            factory = WaveformViewModelFactory(
                CardRepository(context.applicationContext as Application, BackupManager(context.applicationContext as Application)),
                cardId
            )
        )
        val uiState by waveformViewModel.uiState.collectAsState()
        val textMeasurer = rememberTextMeasurer()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            IconButton(onClick = { waveformViewModel.togglePlayback() }) {
                Text(if (uiState.isPlaying) "Stop" else "Play")
            }

            uiState.waveformData?.let { data ->
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            waveformViewModel.onZoom(zoom)
                            val maxPan = (size.width * uiState.zoom) - size.width
                            val newPan = (uiState.panOffset + pan.x).coerceIn(0f, maxPan)
                            waveformViewModel.setPan(newPan)
                        }
                    }
                ) {
                    if (data.isEmpty()) return@Canvas

                    val step = (size.width / data.size) * uiState.zoom
                    if (step == 0f) return@Canvas

                    val start = (uiState.panOffset / step).toInt().coerceIn(0, data.size)
                    val end = (start + (size.width / step).toInt()).coerceIn(0, data.size)

                    for (i in start until end - 1) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(x = (i - start) * step, y = (size.height / 2) - (data[i] * size.height / 2)),
                            end = Offset(x = (i + 1 - start) * step, y = (size.height / 2) - (data[i + 1] * size.height / 2))
                        )
                    }

                    val trackData = uiState.trackData ?: ""
                    val charStep = step * 5 // 5 bits per character for Track 2
                    for (i in trackData.indices) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = trackData[i].toString(),
                            style = TextStyle(color = Color.Black),
                            topLeft = Offset(x = i * charStep - uiState.panOffset, y = size.height - 50)
                        )
                    }
                }
            }
        }
    } else {
        val context = LocalContext.current
        val audioRecordingViewModel: AudioRecordingViewModel = viewModel()
        val selectedFileUri by audioFileViewModel.selectedFileUri.collectAsState()
        var isRecording by remember { mutableStateOf(false) }
        val audioData by audioRecordingViewModel.audioData.collectAsState()
        val errorMessage by audioRecordingViewModel.errorMessage.collectAsState()
        val savedFilePath by audioRecordingViewModel.savedFilePath.collectAsState()
        val availableDevices by audioRecordingViewModel.availableDevices.collectAsState()
        val selectedDevice by audioRecordingViewModel.selectedDevice.collectAsState()
        var expanded by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            audioRecordingViewModel.getAvailableRecordingDevices(context)
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

        val selectFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                uri?.let {
                    audioFileViewModel.onFileSelected(context, it)
                    navController.navigate(Screen.SwipeSelection.route)
                }
            }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = {
                when (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )) {
                    PackageManager.PERMISSION_GRANTED -> {
                        selectFileLauncher.launch("audio/*")
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }) {
                Text("Select Audio File")
            }
            selectedFileUri?.let {
                Text("Selected file: ${it.path}")
            }

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
                    value = selectedDevice?.productName?.toString() ?: "Select a device",
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
                            text = { Text(device.productName.toString()) },
                            onClick = {
                                audioRecordingViewModel.onDeviceSelected(device)
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
                            audioRecordingViewModel.stopRecording()
                        } else {
                            audioRecordingViewModel.startRecording(context, selectedDevice)
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
}
