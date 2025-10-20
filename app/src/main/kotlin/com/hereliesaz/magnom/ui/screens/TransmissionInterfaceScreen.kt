package magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hereliesaz.magnom.viewmodels.TransmissionInterfaceViewModel

@Composable
fun TransmissionInterfaceScreen(
    navController: NavController,
    viewModel: TransmissionInterfaceViewModel = viewModel()
) {
    val cardProfile by viewModel.cardProfile.collectAsState()
    val transmissionStatus by viewModel.transmissionStatus.collectAsState()
    val isTransmitting = transmissionStatus == "Transmitting..."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        cardProfile?.let {
            Text("Ready to Transmit", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Card: ${it.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.transmit() },
                enabled = !isTransmitting
            ) {
                Text("Transmit")
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (transmissionStatus) {
                "Transmitting..." -> CircularProgressIndicator()
                "Transmission successful!" -> Text(
                    "Transmission successful!",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                else -> Text(
                    transmissionStatus,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
