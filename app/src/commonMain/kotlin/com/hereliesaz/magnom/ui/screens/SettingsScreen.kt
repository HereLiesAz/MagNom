package com.hereliesaz.magnom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hereliesaz.magnom.domain.BackupInfo
import com.hereliesaz.magnom.ui.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen() {
    val vm: SettingsViewModel = koinViewModel()
    val appLock by vm.appLockEnabled.collectAsState()
    val backups by vm.backupList.collectAsState()
    val backupMessage by vm.backupMessage.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("App lock", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Require biometric or device credential to open the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = appLock, onCheckedChange = vm::setAppLock)
        }

        HorizontalDivider()

        Text("Transports", style = MaterialTheme.typography.titleMedium)
        vm.transports().forEach { (kind, available) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(kind.name, Modifier.weight(1f), fontFamily = FontFamily.Monospace)
                Text(
                    if (available) "available" else "unavailable",
                    color = if (available) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        HorizontalDivider()

        BackupSection(
            backups = backups,
            message = backupMessage,
            onCreate = vm::createBackup,
            onRestore = vm::restoreBackup,
            onDelete = vm::deleteBackup,
        )

        HorizontalDivider()

        Text(
            "MagNom stores all card data encrypted on this device only. Nothing is transmitted to any server.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackupSection(
    backups: List<BackupInfo>,
    message: String?,
    onCreate: (String) -> Unit,
    onRestore: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Backup & restore", style = MaterialTheme.typography.titleMedium)
        Text(
            "Encrypted with a password of your choice (min 8 chars). Keep the password safe — a backup " +
                "cannot be restored without it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Backup password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onCreate(password) },
            enabled = password.length >= 8,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Create backup") }

        if (backups.isNotEmpty()) {
            Text("Saved backups", style = MaterialTheme.typography.bodyMedium)
            backups.forEach { info ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        info.name,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    TextButton(
                        onClick = { onRestore(info.name, password) },
                        enabled = password.isNotEmpty(),
                    ) { Text("Restore") }
                    TextButton(onClick = { onDelete(info.name) }) { Text("Delete") }
                }
            }
        }

        message?.let {
            Text(
                it,
                color = if (it.contains("failed", ignoreCase = true)) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
