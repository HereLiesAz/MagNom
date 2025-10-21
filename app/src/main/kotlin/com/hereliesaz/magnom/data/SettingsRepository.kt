package com.hereliesaz.magnom.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("magnom_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
        private const val KEY_BACKUP_PASSWORD = "backup_password"
        private const val KEY_BACKUP_LOCATION = "backup_location"
    }

    fun setBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKUP_ENABLED, enabled).apply()
    }

    fun isBackupEnabled(): Boolean {
        return prefs.getBoolean(KEY_BACKUP_ENABLED, false)
    }

    fun setBackupPassword(password: String) {
        prefs.edit().putString(KEY_BACKUP_PASSWORD, password).apply()
    }

    fun getBackupPassword(): String {
        return prefs.getString(KEY_BACKUP_PASSWORD, "") ?: ""
    }

    fun setBackupLocation(location: String) {
        prefs.edit().putString(KEY_BACKUP_LOCATION, location).apply()
    }

    fun getBackupLocation(): String {
        return prefs.getString(KEY_BACKUP_LOCATION, "") ?: ""
    }
}
