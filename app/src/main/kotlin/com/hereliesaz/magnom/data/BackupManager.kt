package com.hereliesaz.magnom.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileOutputStream
import android.net.Uri

class BackupManager(private val context: Context) {

    private val gson = Gson()
    private val settingsRepository = SettingsRepository(context)
    private var backupJob: Job? = null
    private val backupScope = CoroutineScope(Dispatchers.IO)

    fun createBackup(password: String, destinationUri: String) {
        try {
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            val tempZipFile = File(context.cacheDir, "backup.zip")
            val zipFile = ZipFile(tempZipFile, password.toCharArray())
            val zipParameters = ZipParameters()
            zipParameters.isEncryptFiles = true
            zipParameters.encryptionMethod = EncryptionMethod.AES

            zipFile.addFolder(sharedPrefsDir, zipParameters)

            context.contentResolver.openOutputStream(Uri.parse(destinationUri))?.use { outputStream ->
                tempZipFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempZipFile.delete()
        } catch (e: Exception) {
            Log.e("BackupManager", "Error creating backup", e)
        }
    }

    fun restoreBackup(password: String, sourceUri: Uri): Boolean {
        return try {
            val tempZipFile = File(context.cacheDir, "restore.zip")
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(tempZipFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val zipFile = ZipFile(tempZipFile, password.toCharArray())
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            zipFile.extractAll(sharedPrefsDir.absolutePath)
            tempZipFile.delete()
            true
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restoring backup", e)
            false
        }
    }

    fun onDataChanged() {
        backupJob?.cancel()
        backupJob = backupScope.launch {
            delay(1000) // Debounce for 1 second
            if (settingsRepository.isBackupEnabled()) {
                val password = settingsRepository.getBackupPassword()
                val location = settingsRepository.getBackupLocation()
                if (password.isNotEmpty() && location.isNotEmpty()) {
                    createBackup(password, location)
                }
            }
        }
    }
}
