package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.MainViewModel

/**
 * The Main Screen of the application.
 *
 * Displays the list of saved card profiles.
 * Provides entry points for creating new cards and managing existing ones.
 */
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val cardProfiles by viewModel.cardProfiles.collectAsState()

    // Reload data when the screen appears
    LaunchedEffect(Unit) {
        viewModel.loadCardProfiles()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("editor/null") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            Text("Saved Profiles", style = MaterialTheme.typography.headlineSmall)
            LazyColumn {
                items(cardProfiles) { profile ->
                    ListItem(
                        headlineContent = { Text(profile.name, style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text(profile.pan, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Editor.createRoute(profile.id))
                        },
                        trailingContent = {
                            Column {
                                IconButton(onClick = { navController.navigate(Screen.Transmission.createRoute(profile.id)) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Transmit")
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
