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
import com.hereliesaz.magnom.ui.screens.DeviceScreen
import com.hereliesaz.magnom.ui.screens.HelpScreen
import com.hereliesaz.magnom.ui.screens.MainScreen
import com.hereliesaz.magnom.ui.screens.ParseScreen
import com.hereliesaz.magnom.ui.screens.SwipeSelectionScreen
import com.hereliesaz.magnom.ui.screens.TransmissionInterfaceScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import com.hereliesaz.magnom.viewmodels.ParseViewModel

class MainActivity : ComponentActivity() {

    private lateinit var usbCommunicationService: UsbCommunicationService
    private var isUsbServiceBound = false

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
                                    navController.navigate(Screen.AdvancedEditor.route)
                                }
                                azRailItem(id = "devices", text = "Devices") {
                                    navController.navigate(Screen.Devices.route)
                                }
                                azRailItem(id = "bruteforce", text = "Bruteforce") {
                                    navController.navigate(Screen.Bruteforce.route)
                                }
                                azMenuItem(id = "settings", text = "Settings") {
                                    navController.navigate(Screen.Settings.route)
                                }
                                azMenuItem(id = "help", text = "Help") {
                                    val currentRoute =
                                        navController.currentBackStackEntryAsState().value?.destination?.route
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
    }

    override fun onStop() {
        super.onStop()
        if (isUsbServiceBound) {
            unbindService(usbServiceConnection)
            isUsbServiceBound = false
        }
    }
}
