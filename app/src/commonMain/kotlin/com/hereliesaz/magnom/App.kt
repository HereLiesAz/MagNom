package com.hereliesaz.magnom

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hereliesaz.magnom.ui.SettingsViewModel
import com.hereliesaz.magnom.ui.screens.CardEditorScreen
import com.hereliesaz.magnom.ui.screens.CardListScreen
import com.hereliesaz.magnom.ui.screens.ConsentGate
import com.hereliesaz.magnom.ui.screens.RawAnalyzerScreen
import com.hereliesaz.magnom.ui.screens.SettingsScreen
import com.hereliesaz.magnom.ui.screens.TransmitScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

/** In-app navigation. Five destinations, no route-string sentinels. */
sealed interface Route {
    data object List : Route
    data class Editor(val id: String?) : Route
    data object Raw : Route
    data class Transmit(val cardId: String) : Route
    data object Settings : Route
}

@Composable
fun App() {
    MagNomTheme {
        KoinContext {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val settingsVm: SettingsViewModel = koinViewModel()
                val consent by settingsVm.consentAccepted.collectAsState()
                if (!consent) {
                    ConsentGate(onAccept = { settingsVm.setConsent(true) })
                } else {
                    MainScaffold()
                }
            }
        }
    }
}

@Composable
private fun MainScaffold() {
    var route by remember { mutableStateOf<Route>(Route.List) }
    val topLevel = route is Route.List || route is Route.Raw || route is Route.Settings

    Row(Modifier.fillMaxSize()) {
        if (topLevel) {
            NavigationRail {
                NavigationRailItem(
                    selected = route is Route.List,
                    onClick = { route = Route.List },
                    icon = { Text("▤") },
                    label = { Text("Cards") },
                )
                NavigationRailItem(
                    selected = route is Route.Raw,
                    onClick = { route = Route.Raw },
                    icon = { Text("◇") },
                    label = { Text("Analyze") },
                )
                NavigationRailItem(
                    selected = route is Route.Settings,
                    onClick = { route = Route.Settings },
                    icon = { Text("⚙") },
                    label = { Text("Settings") },
                )
            }
        }

        when (val r = route) {
            is Route.List -> CardListScreen(
                onAdd = { route = Route.Editor(null) },
                onEdit = { route = Route.Editor(it) },
                onTransmit = { route = Route.Transmit(it) },
            )
            is Route.Editor -> CardEditorScreen(cardId = r.id, onDone = { route = Route.List })
            is Route.Raw -> RawAnalyzerScreen(onDone = { route = Route.List })
            is Route.Transmit -> TransmitScreen(cardId = r.cardId, onBack = { route = Route.List })
            is Route.Settings -> SettingsScreen()
        }
    }
}
