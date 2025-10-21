package com.hereliesaz.magnom.ui.screens

import android.app.Application
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
import com.hereliesaz.magnom.ui.components.InfoIcon
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hereliesaz.magnom.viewmodels.NavigationEvent
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.CardEditorViewModel
import com.hereliesaz.magnom.viewmodels.CardEditorViewModelFactory

@Composable
fun CardEditorScreen(navController: NavController, cardId: String? = null) {
    val context = LocalContext.current
    val cardEditorViewModel: CardEditorViewModel = viewModel(
        factory = CardEditorViewModelFactory(
            context.applicationContext as Application,
            cardId
        )
    )
    val uiState by cardEditorViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        cardEditorViewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.ToUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(event.url)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.name,
                onValueChange = cardEditorViewModel::onNameChange,
                label = { Text("Name") }
            )
            InfoIcon("Enter the full name of the cardholder.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.pan,
                onValueChange = cardEditorViewModel::onPanChange,
                label = { Text("PAN") },
                isError = uiState.error is com.hereliesaz.magnom.viewmodels.CardEditorError.InvalidPan
            )
            InfoIcon("Enter the Primary Account Number (PAN) of the card.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.expirationDate,
                onValueChange = cardEditorViewModel::onExpirationDateChange,
                label = { Text("Expiration Date (YYMM)") },
                isError = uiState.error is com.hereliesaz.magnom.viewmodels.CardEditorError.InvalidExpirationDate
            )
            InfoIcon("Enter the expiration date in YYMM format.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.serviceCode,
                onValueChange = cardEditorViewModel::onServiceCodeChange,
                label = { Text("Service Code") },
                isError = uiState.error is com.hereliesaz.magnom.viewmodels.CardEditorError.InvalidServiceCode
            )
            InfoIcon("Enter the service code of the card.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.notes,
                onValueChange = cardEditorViewModel::onNotesChange,
                label = { Text("Notes") }
            )
            InfoIcon("Enter any notes for this card.")
        }

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
            Button(onClick = { cardEditorViewModel.smartBackgroundCheck(uiState.name) }) {
                Text("Smart Background Check")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { cardEditorViewModel.geminiDeepResearch() }) {
                Text("Gemini Deep Research")
            }
        }

        uiState.error?.let {
            Text(text = it.message, color = MaterialTheme.colorScheme.error)
        }
    }
}
