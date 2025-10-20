package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.hereliesaz.magnom.audio.Swipe
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
    val cardRepository = CardRepository(context)
    val viewModel: CreateCardProfileViewModel = viewModel(
        factory = CreateCardProfileViewModelFactory(swipe, cardRepository)
    )

    val name by viewModel.name.collectAsState()
    val pan by viewModel.pan.collectAsState()
    val expirationDate by viewModel.expirationDate.collectAsState()
    val serviceCode by viewModel.serviceCode.collectAsState()

    Column {
        TextField(
            value = name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Name") }
        )
        TextField(
            value = pan,
            onValueChange = { viewModel.onPanChange(it) },
            label = { Text("PAN") }
        )
        TextField(
            value = expirationDate,
            onValueChange = { viewModel.onExpirationDateChange(it) },
            label = { Text("Expiration Date") }
        )
        TextField(
            value = serviceCode,
            onValueChange = { viewModel.onServiceCodeChange(it) },
            label = { Text("Service Code") }
        )
        Button(onClick = {
            viewModel.saveCardProfile()
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}
