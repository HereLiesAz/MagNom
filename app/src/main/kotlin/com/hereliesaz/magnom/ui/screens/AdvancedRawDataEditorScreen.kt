package magnom.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.AdvancedRawDataEditorViewModel

@Composable
fun AdvancedRawDataEditorScreen(navController: NavController, viewModel: AdvancedRawDataEditorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(
            value = uiState.rawTrack1,
            onValueChange = viewModel::onTrack1Change,
            label = { Text("Raw Track 1 Data") },
        )
        TextField(
            value = uiState.rawTrack2,
            onValueChange = viewModel::onTrack2Change,
            label = { Text("Raw Track 2 Data") },
            isError = !uiState.isTrack2Valid
        )

        if (uiState.isTrack2Valid && uiState.parsedTrack2Data != null) {
            Text("Parsed Track 2 Data:")
            Text("PAN: ${uiState.parsedTrack2Data?.pan}")
            Text("Expiration: ${uiState.parsedTrack2Data?.expirationDate}")
            Text("Service Code: ${uiState.parsedTrack2Data?.serviceCode}")
        } else if (!uiState.isTrack2Valid) {
            Text("Invalid Track 2 Data", color = Color.Red)
        }
    }
}
