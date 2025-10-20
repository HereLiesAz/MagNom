package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.TransmissionInterfaceViewModel

@Composable
fun TransmissionInterfaceScreen(navController: NavController, viewModel: TransmissionInterfaceViewModel = viewModel()) {
    val cardProfile by viewModel.cardProfile.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        cardProfile?.let {
            Text("Name: ${it.name}")
            Text("PAN: ${it.pan}")
            Text("Expiration: ${it.expirationDate}")
            Text("Service Code: ${it.serviceCode}")
            Button(onClick = { viewModel.transmit() }) {
                Text("Transmit")
            }
        }
    }
}
