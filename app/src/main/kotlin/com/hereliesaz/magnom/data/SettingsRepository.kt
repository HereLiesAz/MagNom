package com.hereliesaz.magnom.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

@Suppress("DEPRECATION")
class SettingsRepository(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "user_settings",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setDataSharing(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("data_sharing_enabled", enabled)
            apply()
        }
    }

    fun isDataSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean("data_sharing_enabled", false)
    }

    fun setBackupPassword(password: String) {
        with(sharedPreferences.edit()) {
            putString("backup_password", password)
            apply()
        }
    }

    fun getBackupPassword(): String {
        return sharedPreferences.getString("backup_password", "") ?: ""
    }

    fun setBackupLocation(location: String) {
        with(sharedPreferences.edit()) {
            putString("backup_location", location)
            apply()
        }
    }

    fun getBackupLocation(): String {
        return sharedPreferences.getString("backup_location", "") ?: ""
    }

    fun setBackupEnabled(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("backup_enabled", enabled)
            apply()
        }
    }

    fun isBackupEnabled(): Boolean {
        return sharedPreferences.getBoolean("backup_enabled", false)
    }
}
