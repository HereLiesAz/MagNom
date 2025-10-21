package com.hereliesaz.magnom.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun InfoIcon(
    infoText: String,
    icon: ImageVector = Icons.Default.Info
) {
    val openDialog = remember { mutableStateOf(false) }

    IconButton(onClick = { openDialog.value = true }) {
        Icon(icon, contentDescription = "Info")
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Info") },
            text = { Text(infoText) },
            confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}
