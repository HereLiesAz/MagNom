package com.hereliesaz.magnom.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson

@Suppress("DEPRECATION")
class CardRepository(context: Context, private val backupManager: BackupManager? = null) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val gson = Gson()

    private fun onDataChanged() {
        backupManager?.onDataChanged()
    }

    fun saveCardProfile(profile: CardProfile) {
        if (profile.pan.isBlank() || profile.name.isBlank() || profile.expirationDate.isBlank() || profile.serviceCode.isBlank()) {
            return // Or throw an exception
        }
        val json = gson.toJson(profile)
        sharedPreferences.edit().putString(profile.id, json).apply()
        onDataChanged()
    }

    fun getCardProfile(id: String): CardProfile? {
        val json = sharedPreferences.getString(id, null)
        return gson.fromJson(json, CardProfile::class.java)
    }

    fun getAllCardProfiles(): List<CardProfile> {
        return sharedPreferences.all.mapNotNull {
            val json = it.value as? String
            json?.let { gson.fromJson(it, CardProfile::class.java) }
        }
    }

    fun deleteCardProfile(id: String) {
        sharedPreferences.edit().remove(id).apply()
        onDataChanged()
    }
}
