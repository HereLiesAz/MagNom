package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.CardEditorViewModel

@Composable
fun CardEditorScreen(navController: NavController, cardEditorViewModel: CardEditorViewModel = viewModel()) {
    val uiState by cardEditorViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = uiState.name,
            onValueChange = cardEditorViewModel::onNameChange,
            label = { Text("Name") }
        )
        TextField(
            value = uiState.pan,
            onValueChange = cardEditorViewModel::onPanChange,
            label = { Text("PAN") }
        )
        TextField(
            value = uiState.expirationDate,
            onValueChange = cardEditorViewModel::onExpirationDateChange,
            label = { Text("Expiration Date (YYMM)") }
        )
        TextField(
            value = uiState.serviceCode,
            onValueChange = cardEditorViewModel::onServiceCodeChange,
            label = { Text("Service Code") }
        )
        Button(onClick = {
            cardEditorViewModel.saveCardProfile()
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}
