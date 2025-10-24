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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModel
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModelFactory

@Composable
fun CardEditorScreen(navController: NavController, cardId: String? = null) {
    val context = LocalContext.current
    val createCardProfileViewModel: CreateCardProfileViewModel = viewModel(
        factory = CreateCardProfileViewModelFactory(
            context.applicationContext as Application,
            cardId
        )
    )
    val uiState by createCardProfileViewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        createCardProfileViewModel.onFrontImageUriChange(uri)
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        createCardProfileViewModel.onBackImageUriChange(uri)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.name,
                onValueChange = createCardProfileViewModel::onNameChange,
                label = { Text("Name") },
                textStyle = MaterialTheme.typography.bodySmall
            )
            InfoIcon("Enter the full name of the cardholder.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.pan,
                onValueChange = createCardProfileViewModel::onPanChange,
                label = { Text("PAN") },
                textStyle = MaterialTheme.typography.bodySmall
            )
            InfoIcon("Enter the Primary Account Number (PAN) of the card.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.expirationDate,
                onValueChange = createCardProfileViewModel::onExpirationDateChange,
                label = { Text("Expiration Date (YYMM)") },
                textStyle = MaterialTheme.typography.bodySmall
            )
            InfoIcon("Enter the expiration date in YYMM format.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.serviceCode,
                onValueChange = createCardProfileViewModel::onServiceCodeChange,
                label = { Text("Service Code") },
                textStyle = MaterialTheme.typography.bodySmall
            )
            InfoIcon("Enter the service code of the card.")
        }

        uiState.notes.forEachIndexed { index, note ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = note,
                    onValueChange = { createCardProfileViewModel.onNoteChange(index, it) },
                    label = { Text("Note ${index + 1}") },
                    textStyle = MaterialTheme.typography.bodySmall
                )
                IconButton(onClick = { createCardProfileViewModel.removeNote(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Note")
                }
            }
        }

        IconButton(onClick = { createCardProfileViewModel.addNote() }) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Select Front Image", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { backLauncher.launch("image/*") }) {
                Text("Select Back Image", style = MaterialTheme.typography.bodySmall)
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

        Button(onClick = {
            createCardProfileViewModel.saveCardProfile()
            navController.popBackStack()
        }) {
            Text("Save", style = MaterialTheme.typography.bodySmall)
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
