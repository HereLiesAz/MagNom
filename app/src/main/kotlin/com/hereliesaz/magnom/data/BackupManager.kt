package com.hereliesaz.magnom.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.hereliesaz.magnom.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileOutputStream

/**
 * Manages the backup and restore processes for application data.
 *
 * This class handles creating password-protected ZIP archives of the application's
 * shared preferences (where card data is stored) and restoring them. It also
 * manages an auto-backup feature with debouncing.
 *
 * @property context The application context.
 * @property settingsRepository Repository to access backup settings (password, location).
 */
class BackupManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository = SettingsRepository(context)
) {
    // Coroutine scope for background backup tasks
    private val backupScope = CoroutineScope(Dispatchers.IO)
    // Job reference for debouncing auto-backups
    private var backupJob: Job? = null

    /**
     * Creates a secure backup of the app's shared preferences.
     *
     * @param password The password to encrypt the ZIP file with.
     * @param destinationUriString The URI string where the backup file should be saved.
     * @return [Result.Success] if successful, or [Result.Error] if failed.
     */
    suspend fun createBackup(password: String, destinationUriString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Locate the shared preferences directory
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (!sharedPrefsDir.exists()) {
                return@withContext Result.Error("No data to backup")
            }

            // Parse the destination URI
            val destinationUri = Uri.parse(destinationUriString)

            // Create a temporary ZIP file in the cache directory
            val tempZipFile = File(context.cacheDir, "backup.zip")
            if (tempZipFile.exists()) tempZipFile.delete()

            // Configure ZIP parameters: AES encryption, Maximum compression
            val zipParameters = ZipParameters().apply {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
                compressionLevel = CompressionLevel.MAXIMUM
            }

            // Create the ZIP file and add the shared_prefs directory
            val zipFile = ZipFile(tempZipFile, password.toCharArray())
            zipFile.addFolder(sharedPrefsDir, zipParameters)

            // Copy the temporary ZIP file to the user-selected destination
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                tempZipFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Clean up the temporary file
            tempZipFile.delete()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error creating backup", e)
            Result.Error("Error creating backup: ${e.message}")
        }
    }

    /**
     * Restores application data from a backup file.
     *
     * @param password The password to decrypt the backup.
     * @param sourceUri The URI of the backup file.
     * @return [Result.Success] if successful, or [Result.Error] if failed.
     */
    suspend fun restoreBackup(password: String, sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Copy the source file to a temporary location
            val tempZipFile = File(context.cacheDir, "restore.zip")
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(tempZipFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Open the ZIP file with the provided password
            val zipFile = ZipFile(tempZipFile, password.toCharArray())
            if (!zipFile.isValidZipFile) {
                return@withContext Result.Error("Invalid backup file")
            }

            // Extract to a temporary directory first to validate contents
            val tempExtractDir = File(context.cacheDir, "restore_temp")
            if (tempExtractDir.exists()) tempExtractDir.deleteRecursively()
            tempExtractDir.mkdirs()

            zipFile.extractAll(tempExtractDir.absolutePath)

            // Validate that we only have expected files (e.g. only in a folder that looks like shared_prefs or root files that look like XMLs)
            // Ideally, backups created by this app put 'shared_prefs' folder at root of zip.
            // Let's check if 'shared_prefs' exists in the extracted root.
            val extractedSharedPrefs = File(tempExtractDir, "shared_prefs")
            if (extractedSharedPrefs.exists() && extractedSharedPrefs.isDirectory) {
                // Move valid files to actual data dir
                val targetSharedPrefs = File(context.applicationInfo.dataDir, "shared_prefs")
                if (!targetSharedPrefs.exists()) targetSharedPrefs.mkdirs()

                extractedSharedPrefs.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension == "xml") {
                        file.copyTo(File(targetSharedPrefs, file.name), overwrite = true)
                    }
                }
            } else {
                // Handle case where files might be at root (legacy or different structure)
                // Just copy XMLs to shared_prefs if they look like shared prefs?
                // Or fail if structure isn't as expected (safer).
                // Given createBackup uses zipFile.addFolder(sharedPrefsDir), the zip root will contain "shared_prefs" folder.
                return@withContext Result.Error("Invalid backup structure: missing shared_prefs")
            }

            // Clean up
            tempZipFile.delete()
            tempExtractDir.deleteRecursively()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restoring backup", e)
            Result.Error("Error restoring backup: ${e.message}")
        }
    }

    /**
     * Triggers a debounced auto-backup if enabled.
     *
     * This method should be called whenever data changes. It waits for 1 second of inactivity
     * before performing the backup to avoid excessive I/O during rapid changes.
     */
    fun onDataChanged() {
        // Cancel any pending backup job
        backupJob?.cancel()
        // Start a new backup job
        backupJob = backupScope.launch {
            delay(1000) // Debounce for 1 second

            // Check if backup is enabled and configured
            if (settingsRepository.isBackupEnabled()) {
                val password = settingsRepository.getBackupPassword()
                val location = settingsRepository.getBackupLocation()

                // Perform the backup if configuration is valid
                if (password.isNotEmpty() && location.isNotEmpty()) {
                    createBackup(password, location)
                }
            }
        }
    }
}
