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
import com.hereliesaz.magnom.audio.Result

class BackupManager(private val context: Context) {

    private val gson = Gson()
    private val settingsRepository = SettingsRepository(context)
    private var backupJob: Job? = null
    private val backupScope = CoroutineScope(Dispatchers.IO)

    fun createBackup(password: String, destinationUri: String): Result<Unit> {
        return try {
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            val tempZipFile = File.createTempFile("backup", ".zip", context.cacheDir)
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
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error creating backup", e)
            Result.Error("Error creating backup: ${e.message}")
        }
    }

    fun restoreBackup(password: String, sourceUri: Uri): Result<Unit> {
        return try {
            val tempZipFile = File.createTempFile("restore", ".zip", context.cacheDir)
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(tempZipFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val zipFile = ZipFile(tempZipFile, password.toCharArray())
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            zipFile.extractAll(sharedPrefsDir.absolutePath)
            tempZipFile.delete()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restoring backup", e)
            Result.Error("Error restoring backup: ${e.message}")
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
