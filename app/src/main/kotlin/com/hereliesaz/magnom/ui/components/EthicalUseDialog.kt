package com.hereliesaz.magnom.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hereliesaz.magnom.data.SettingsRepository

@Composable
fun EthicalUseDialog(onDismiss: () -> Unit) {
    val openDialog = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    if (openDialog.value && !settingsRepository.isDataSharingEnabled()) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
                onDismiss()
            },
            title = { Text("Ethical Use Disclaimer") },
            text = {
                Text(
                    "The devices and techniques described in this app are powerful tools " +
                            "intended strictly for authorized security auditing, academic research, " +
                            "and educational purposes. The emulation of magnetic stripe data " +
                            "without the explicit, legal authorization of the card owner and the " +
                            "system operator is illegal in most jurisdictions and constitutes fraud.\n\n" +
                            "Would you like to share anonymous usage data with the developer to " +
                            "help improve the app?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsRepository.setDataSharing(true)
                        openDialog.value = false
                        onDismiss()
                    }
                ) {
                    Text("Agree")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        settingsRepository.setDataSharing(false)
                        openDialog.value = false
                        onDismiss()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
