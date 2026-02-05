package com.hereliesaz.magnom.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

/**
 * Repository for managing the persistence of [CardProfile] objects.
 *
 * This repository uses Android's [EncryptedSharedPreferences] to securely store
 * card profiles on the device's storage. It handles serialization (JSON) and
 * triggers auto-backups when data changes.
 *
 * @param context The application context.
 * @property backupManager Optional instance of [BackupManager] to notify on data changes.
 */
class CardRepository(
    context: Context,
    private val backupManager: BackupManager? = null
) {

    // Generate or retrieve the Master Key for encryption
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Initialize EncryptedSharedPreferences with AES-256 encryption for both keys and values
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Gson instance for JSON serialization/deserialization
    private val gson = Gson()

    /**
     * Notifies the backup manager that data has changed.
     */
    private fun onDataChanged() {
        backupManager?.onDataChanged()
    }

    /**
     * Saves or updates a card profile in secure storage.
     *
     * @param profile The [CardProfile] to save.
     */
    fun saveCardProfile(profile: CardProfile) {
        // Validate required fields are not blank
        if (profile.pan.isBlank() || profile.name.isBlank() || profile.expirationDate.isBlank() || profile.serviceCode.isBlank()) {
            return // Or throw an exception
        }

        // Serialize the profile to JSON
        val json = gson.toJson(profile)

        // Save to SharedPreferences using the profile ID as the key
        sharedPreferences.edit().putString(profile.id, json).apply()

        // Trigger auto-backup
        onDataChanged()
    }

    /**
     * Retrieves a card profile by its ID.
     *
     * @param id The unique identifier of the card.
     * @return The [CardProfile] if found, or null.
     */
    fun getCardProfile(id: String): CardProfile? {
        val json = sharedPreferences.getString(id, null)
        return gson.fromJson(json, CardProfile::class.java)
    }

    /**
     * Retrieves all saved card profiles.
     *
     * @return A list of all [CardProfile] objects.
     */
    fun getAllCardProfiles(): List<CardProfile> {
        return sharedPreferences.all.mapNotNull {
            // Safely cast the value to String (JSON)
            val json = it.value as? String
            // Deserialize JSON to CardProfile, ignoring invalid entries
            json?.let { gson.fromJson(it, CardProfile::class.java) }
        }
    }

    /**
     * Deletes a card profile by its ID.
     *
     * @param id The unique identifier of the card to delete.
     */
    fun deleteCardProfile(id: String) {
        sharedPreferences.edit().remove(id).apply()
        onDataChanged()
    }
}
