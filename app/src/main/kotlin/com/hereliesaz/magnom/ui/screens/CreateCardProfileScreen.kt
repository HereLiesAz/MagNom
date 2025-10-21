package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.app.Application
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.hereliesaz.magnom.audio.Swipe
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModel
import com.hereliesaz.magnom.viewmodels.CreateCardProfileViewModelFactory

@Composable
fun CreateCardProfileScreen(
    navController: NavController,
    swipeData: String
) {
    val swipe = Gson().fromJson(swipeData, Swipe::class.java)
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: CreateCardProfileViewModel = viewModel(
        factory = CreateCardProfileViewModelFactory(application, swipe, CardRepository(context, BackupManager(context)))
    )

    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onFrontImageUriChange(uri)
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onBackImageUriChange(uri)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        TextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Name") }
        )
        TextField(
            value = uiState.pan,
            onValueChange = { viewModel.onPanChange(it) },
            label = { Text("PAN") }
        )
        TextField(
            value = uiState.expirationDate,
            onValueChange = { viewModel.onExpirationDateChange(it) },
            label = { Text("Expiration Date") }
        )
        TextField(
            value = uiState.serviceCode,
            onValueChange = { viewModel.onServiceCodeChange(it) },
            label = { Text("Service Code") }
        )
        TextField(
            value = uiState.notes,
            onValueChange = { viewModel.onNotesChange(it) },
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

        Button(onClick = {
            viewModel.saveCardProfile()
            navController.popBackStack()
        }) {
            Text("Save")
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
