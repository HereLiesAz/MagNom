package com.hereliesaz.magnom.ui.screens

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModel
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModelFactory

/**
 * Screen for creating or editing a card profile.
 *
 * Provides a form for entering card details, capturing photos (front/back), and managing notes.
 * Supports OCR extraction from photos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(
    navController: NavController,
    cardId: String? = null
) {
    val context = LocalContext.current
    val viewModel: CreateCardProfileViewModel = viewModel(
        factory = CreateCardProfileViewModelFactory(context.applicationContext as Application, cardId)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Photo pickers
    val frontImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onFrontImageUriChange(uri)
    }

    val backImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onBackImageUriChange(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "Create Profile" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Photo Buttons
                Row {
                    Button(onClick = {
                        frontImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Front Photo")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        backImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Back Photo")
                    }
                }

                // Image Previews
                if (uiState.frontImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.frontImageUri),
                        contentDescription = "Front Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                if (uiState.backImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.backImageUri),
                        contentDescription = "Back Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.pan,
                    onValueChange = viewModel::onPanChange,
                    label = { Text("PAN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.expirationDate,
                    onValueChange = viewModel::onExpirationDateChange,
                    label = { Text("Expiration Date (MM/YY)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.serviceCode,
                    onValueChange = viewModel::onServiceCodeChange,
                    label = { Text("Service Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Notes", style = MaterialTheme.typography.titleMedium)
            }

            // Dynamic list of notes
            itemsIndexed(uiState.notes) { index, note ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { viewModel.onNoteChange(index, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Note ${index + 1}") }
                    )
                    IconButton(onClick = { viewModel.removeNote(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Note")
                    }
                }
            }

            item {
                Button(onClick = { viewModel.addNote() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Note")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.saveCardProfile()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Profile")
                }
                if (uiState.error != null) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
