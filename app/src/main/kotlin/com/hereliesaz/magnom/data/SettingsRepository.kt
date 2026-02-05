package com.hereliesaz.magnom.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Repository for managing application settings and preferences.
 *
 * This repository handles the persistence of user configurations such as
 * data sharing consent and backup settings using [EncryptedSharedPreferences].
 *
 * @param context The application context.
 */
@Suppress("DEPRECATION")
class SettingsRepository(context: Context) {

    // Retrieve or create the Master Key Alias (using deprecated MasterKeys for compatibility if needed,
    // though MasterKey.Builder is preferred in newer code).
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Initialize EncryptedSharedPreferences for user settings
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "user_settings",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Updates the data sharing (analytics) preference.
     *
     * @param enabled True to enable data sharing, false to disable.
     */
    fun setDataSharing(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("data_sharing_enabled", enabled)
            apply()
        }
    }

    /**
     * Checks if data sharing is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    fun isDataSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean("data_sharing_enabled", false)
    }

    /**
     * Sets the password used for encrypting backups.
     *
     * @param password The user-provided password.
     */
    fun setBackupPassword(password: String) {
        with(sharedPreferences.edit()) {
            putString("backup_password", password)
            apply()
        }
    }

    /**
     * Retrieves the backup password.
     *
     * @return The password string, or empty if not set.
     */
    fun getBackupPassword(): String {
        return sharedPreferences.getString("backup_password", "") ?: ""
    }

    /**
     * Sets the target location (URI string) for backups.
     *
     * @param location The URI string of the backup file/directory.
     */
    fun setBackupLocation(location: String) {
        with(sharedPreferences.edit()) {
            putString("backup_location", location)
            apply()
        }
    }

    /**
     * Retrieves the backup location.
     *
     * @return The URI string, or empty if not set.
     */
    fun getBackupLocation(): String {
        return sharedPreferences.getString("backup_location", "") ?: ""
    }

    /**
     * Enables or disables the auto-backup feature.
     *
     * @param enabled True to enable, false to disable.
     */
    fun setBackupEnabled(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("backup_enabled", enabled)
            apply()
        }
    }

    /**
     * Checks if auto-backup is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    fun isBackupEnabled(): Boolean {
        return sharedPreferences.getBoolean("backup_enabled", false)
    }
}
