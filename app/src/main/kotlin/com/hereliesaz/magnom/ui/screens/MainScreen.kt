package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hereliesaz.magnom.ui.components.EthicalUseDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.viewmodels.MainViewModel

@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val cardProfiles by mainViewModel.cardProfiles.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val showDialog = remember { mutableStateOf(true) }

    if (showDialog.value) {
        EthicalUseDialog(onDismiss = { showDialog.value = false })
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.loadCardProfiles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.Editor.createRoute(null)) }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            if (cardProfiles.isEmpty()) {
                Text(
                    text = "No card profiles saved. \n Click the '+' button to add one.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn {
                    items(cardProfiles) { profile ->
                        ListItem(
                            headlineContent = { Text(profile.name) },
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Transmission.createRoute(profile.id))
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
