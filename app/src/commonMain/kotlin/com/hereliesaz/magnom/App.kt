package com.hereliesaz.magnom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.aznavrail.AzAppMeta
import com.hereliesaz.aznavrail.AzHostActivityLayout
import com.hereliesaz.aznavrail.LocalAzAppMeta
import com.hereliesaz.magnom.ui.SettingsViewModel
import com.hereliesaz.magnom.ui.screens.CardEditorScreen
import com.hereliesaz.magnom.ui.screens.CardListScreen
import com.hereliesaz.magnom.ui.screens.ConsentGate
import com.hereliesaz.magnom.ui.screens.HelpScreen
import com.hereliesaz.magnom.ui.screens.RawAnalyzerScreen
import com.hereliesaz.magnom.ui.screens.SettingsScreen
import com.hereliesaz.magnom.ui.screens.TransmitScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

/** In-app navigation. Typed routes; no route-string sentinels. */
sealed interface Route {
    /** Rail item id this route highlights. Detail routes highlight their parent. */
    val railId: String

    /** Title shown by the rail host; screens draw no heading of their own. */
    val title: String

    data object List : Route { override val railId = "cards"; override val title = "Cards" }
    data class Editor(val id: String?) : Route {
        override val railId = "cards"
        override val title = if (id == null) "New Card" else "Edit Card"
    }
    data object Raw : Route { override val railId = "analyze"; override val title = "Raw Analyzer" }
    data class Transmit(val cardId: String) : Route { override val railId = "cards"; override val title = "Transmit" }
    data object Settings : Route { override val railId = "settings"; override val title = "Settings" }
    data object Help : Route { override val railId = "help"; override val title = "Help" }
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

/**
 * Root shell: AzNavRail (Compose Multiplatform) hosts the rail and the active screen.
 *
 * Navigation state stays in our typed [Route]. Rail items carry no nav-graph route (the
 * NavController exists only because the host requires one); the active item is lit by matching
 * its classifier against [Route.railId] via `azConfig(activeClassifiers)`.
 */
@Composable
private fun MainScaffold() {
    var route by remember { mutableStateOf<Route>(Route.List) }
    val navController = rememberNavController()
    val accent = MaterialTheme.colorScheme.primary

    CompositionLocalProvider(
        LocalAzAppMeta provides AzAppMeta(name = "MagNom", packageId = "com.hereliesaz.magnom"),
    ) {
        AzHostActivityLayout(navController = navController) {
            azConfig(activeClassifiers = setOf(route.railId))
            azTheme(activeColor = accent)

            azRailItem(id = "cards", text = "Cards", classifiers = setOf("cards"),
                screenTitle = if (route.railId == "cards") route.title else "Cards") {
                route = Route.List
            }
            azRailItem(id = "analyze", text = "Analyze", classifiers = setOf("analyze"), screenTitle = "Raw Analyzer") {
                route = Route.Raw
            }
            azRailItem(id = "settings", text = "Settings", classifiers = setOf("settings"), screenTitle = "Settings") {
                route = Route.Settings
            }
            azRailItem(id = "help", text = "Help", classifiers = setOf("help"), screenTitle = "Help") {
                route = Route.Help
            }

            onscreen {
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
                    is Route.Help -> HelpScreen()
                }
            }
        }
    }
}
