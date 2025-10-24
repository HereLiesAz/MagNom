package com.hereliesaz.magnom.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.services.BleCommunicationService
import com.hereliesaz.magnom.viewmodels.MagspoofReplayViewModel
import com.hereliesaz.magnom.viewmodels.MagspoofReplayViewModelFactory
import androidx.compose.runtime.LaunchedEffect

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagspoofReplayScreen(
    navController: NavController,
    bleCommunicationService: BleCommunicationService
) {
    val context = LocalContext.current
    val cardRepository = CardRepository(context, BackupManager(context))
    val viewModel: MagspoofReplayViewModel = viewModel(
        factory = MagspoofReplayViewModelFactory(bleCommunicationService, cardRepository)
    )
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val transmissionStatus by viewModel.transmissionStatus.collectAsState()

    val selectedCardId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("selectedCardId", null)
        ?.collectAsState()

    LaunchedEffect(selectedCardId?.value) {
        viewModel.setSelectedCard(selectedCardId?.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Magspoof Replay") },
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
                Button(onClick = { viewModel.startScan() }) {
                    Text("Start Scan")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.stopScan() }) {
                    Text("Stop Scan")
                }
            }
            Text("Connection State: $connectionState")
            Text("Transmission Status: $transmissionStatus")
            Text("Selected Card: ${viewModel.selectedCard.collectAsState().value?.name ?: "None"}")
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
            Button(onClick = { navController.navigate(Screen.CardSelection.route) }) {
                Text("Select Card")
            }
            Button(
                onClick = {
                    viewModel.selectedCard.value?.let {
                        viewModel.writeTrackData(it.track1, it.track2)
                        viewModel.sendTransmitCommand()
                    }
                },
                enabled = viewModel.selectedCard.collectAsState().value != null
            ) {
                Text("Transmit")
            }
        }
    }
}
