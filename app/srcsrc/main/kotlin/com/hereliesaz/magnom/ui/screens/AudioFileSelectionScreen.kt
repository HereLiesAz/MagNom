package com.hereliesaz.magnom.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.AudioViewModel

@Composable
fun AudioFileSelectionScreen(
    navController: NavController,
    audioViewModel: AudioViewModel = viewModel()
) {
    val selectedFileUri by audioViewModel.selectedFileUri.collectAsState()
    val context = LocalContext.current

    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                audioViewModel.onFileSelected(context, it)
                navController.navigate(Screen.SwipeSelection.route)
            }
        }
    )

    Column {
        Button(onClick = { selectFileLauncher.launch("audio/*") }) {
            Text("Select Audio File")
        }
        selectedFileUri?.let {
            Text("Selected file: ${it.path}")
        }
    }
}
