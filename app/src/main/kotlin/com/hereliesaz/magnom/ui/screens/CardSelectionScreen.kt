package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectionScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val cardProfiles by mainViewModel.cardProfiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Card") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Select a card to view its waveform",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cardProfiles) { profile ->
                    ListItem(
                        headlineContent = { Text(profile.name) },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Waveform.createRoute(profile.id))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
