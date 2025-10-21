package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.CardEditorViewModel

@Composable
fun CardEditorScreen(navController: NavController, cardEditorViewModel: CardEditorViewModel = viewModel()) {
    val uiState by cardEditorViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        cardEditorViewModel.onFrontImageUriChange(uri)
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        cardEditorViewModel.onBackImageUriChange(uri)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
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
        TextField(
            value = uiState.notes,
            onValueChange = cardEditorViewModel::onNotesChange,
            label = { Text("Notes") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Select Front Image")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { backLauncher.launch("image/*") }) {
                Text("Select Back Image")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            uiState.frontImageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Front of card",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            uiState.backImageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Back of card",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row {
            Button(onClick = {
                cardEditorViewModel.saveCardProfile()
                navController.popBackStack()
            }) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { cardEditorViewModel.smartBackgroundCheck(context, uiState.name) }) {
                Text("Smart Background Check")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { cardEditorViewModel.geminiDeepResearch(context) }) {
                Text("Gemini Deep Research")
            }
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
