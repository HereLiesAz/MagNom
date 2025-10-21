package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.MainViewModel

@Composable
fun CardSelectionScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val cardProfiles by mainViewModel.cardProfiles.collectAsState()

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
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
