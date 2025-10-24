package com.hereliesaz.magnom

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.aznavrail.AzNavRail
import com.hereliesaz.aznavrail.model.AzButtonShape
import com.hereliesaz.magnom.data.DeviceRepository
import com.hereliesaz.magnom.navigation.Screen
import com.hereliesaz.magnom.services.UsbCommunicationService
import com.hereliesaz.magnom.ui.screens.BruteforceScreen
import com.hereliesaz.magnom.ui.screens.CardEditorScreen
import com.hereliesaz.magnom.ui.screens.CardSelectionScreen
import com.hereliesaz.magnom.ui.screens.CreateCardProfileScreen
import com.hereliesaz.magnom.ui.screens.AdvancedEditorScreen
import com.hereliesaz.magnom.ui.screens.DeviceScreen
import com.hereliesaz.magnom.ui.screens.HelpScreen
import com.hereliesaz.magnom.ui.screens.MainScreen
import com.hereliesaz.magnom.ui.screens.MagspoofReplayScreen
import com.hereliesaz.magnom.ui.screens.ParseScreen
import com.hereliesaz.magnom.ui.screens.SettingsScreen
import com.hereliesaz.magnom.ui.screens.SwipeSelectionScreen
import com.hereliesaz.magnom.ui.screens.TransmissionInterfaceScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import com.hereliesaz.magnom.viewmodels.ParseViewModel
import com.hereliesaz.magnom.services.BleCommunicationService

class MainActivity : ComponentActivity() {

    private lateinit var usbCommunicationService: UsbCommunicationService
    private var isUsbServiceBound = false
    private lateinit var bleCommunicationService: BleCommunicationService
    private var isBleServiceBound = false

    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleCommunicationService = (service as BleCommunicationService.LocalBinder).getService()
            isBleServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBleServiceBound = false
        }
    }

    private val usbServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            usbCommunicationService = (service as UsbCommunicationService.UsbBinder).getService()
            isUsbServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isUsbServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagNomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val parseViewModel: ParseViewModel = viewModel()
                    val deviceRepository = remember { DeviceRepository() }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                        Row(modifier = Modifier.weight(1f)) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            AzNavRail {
                                azRailItem(id = "main", text = "Main", screenTitle = "Card Profiles") {
                                    navController.navigate(Screen.Main.route)
                                }
                                azRailItem(id = "parse", text = "Parse") {
                                    navController.navigate("parse/null")
                                }
                                azRailItem(id = "editor", text = "Editor") {
                                    navController.navigate("editor/null")
                                }
                                azRailItem(
                                    id = "magspoof_replay",
                                    text = "Replay",
                                    shape = AzButtonShape.NONE
                                ) {
                                    navController.navigate(Screen.MagspoofReplay.route)
                                }
                                azRailItem(id = "advanced_editor", text = "Advanced") {
                                     navController.navigate(Screen.AdvancedEditor.createRoute(null))
                                }
                                azRailItem(id = "devices", text = "Devices") {
                                    navController.navigate(Screen.Devices.route)
                                }
                                azRailItem(id = "bruteforce", text = "Bruteforce") {
                                    navController.navigate(Screen.Bruteforce.route)
                                }
                                azRailItem(id = "settings", text = "Settings", screenTitle = "Settings") {
                                    navController.navigate(Screen.Settings.route)
                                }
                                azRailItem(id = "help", text = "Help") {
                                    val currentRoute = navBackStackEntry?.destination?.route
                                    if (currentRoute != null) {
                                        navController.navigate(Screen.Help.createRoute(currentRoute))
                                    }
                                }
                            }
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Main.route
                            ) {
                                composable(Screen.Main.route) {
                                    MainScreen(navController = navController)
                                }
                                composable(Screen.CardSelection.route) {
                                    CardSelectionScreen(navController = navController)
                                }
                                composable("parse/{cardId}") { backStackEntry ->
                                    val cardId = backStackEntry.arguments?.getString("cardId")
                                    ParseScreen(
                                        navController = navController,
                                        cardId = if (cardId == "null") null else cardId,
                                        parseViewModel = parseViewModel
                                    )
                                }
                                composable(Screen.SwipeSelection.route) {
                                    SwipeSelectionScreen(
                                        navController = navController,
                                        parseViewModel = parseViewModel
                                    )
                                }
                                composable(Screen.CreateCardProfile.route) { backStackEntry ->
                                    val swipeData = backStackEntry.arguments?.getString("swipeData")
                                    if (swipeData != null) {
                                        CreateCardProfileScreen(navController = navController, swipeData = swipeData)
                                    }
                                }
                                composable("editor/{cardId}") { backStackEntry ->
                                    val cardId = backStackEntry.arguments?.getString("cardId")
                                    CardEditorScreen(
                                        navController = navController,
                                        cardId = if (cardId == "null") null else cardId
                                    )
                                }
                                composable("help/{route}") { backStackEntry ->
                                    val route = backStackEntry.arguments?.getString("route")
                                    if (route != null) {
                                        HelpScreen(route = route)
                                    }
                                }
                                composable(Screen.Devices.route) {
                                    DeviceScreen()
                                }
                                composable(Screen.Bruteforce.route) {
                                    BruteforceScreen()
                                }
                                composable(Screen.AdvancedEditor.route) { backStackEntry ->
                                    val cardId = backStackEntry.arguments?.getString("cardId")
                                    AdvancedEditorScreen(
                                        navController = navController,
                                        cardId = if (cardId == "null") null else cardId
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    if (isBleServiceBound) {
                                        SettingsScreen(
                                            navController = navController,
                                            bleCommunicationService = bleCommunicationService
                                        )
                                    }
                                }
                                composable(Screen.MagspoofReplay.route) {
                                    if (isBleServiceBound) {
                                        MagspoofReplayScreen(
                                            navController = navController,
                                            bleCommunicationService = bleCommunicationService
                                        )
                                    }
                                }
                                composable("transmission/{cardId}") { backStackEntry ->
                                    val cardId = backStackEntry.arguments?.getString("cardId")
                                    if (cardId != null) {
                                        TransmissionInterfaceScreen(
                                            navController = navController,
                                            cardId = cardId,
                                            deviceRepository = deviceRepository,
                                            usbCommunicationService = usbCommunicationService
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, UsbCommunicationService::class.java).also { intent ->
            bindService(intent, usbServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(this, BleCommunicationService::class.java).also { intent ->
            bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isUsbServiceBound) {
            unbindService(usbServiceConnection)
            isUsbServiceBound = false
        }
        if (isBleServiceBound) {
            unbindService(bleServiceConnection)
            isBleServiceBound = false
        }
    }
}
