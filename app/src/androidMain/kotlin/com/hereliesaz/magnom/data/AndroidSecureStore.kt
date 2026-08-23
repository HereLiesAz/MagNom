package com.hereliesaz.magnom.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hereliesaz.magnom.domain.SecureStore

/** SecureStore backed by EncryptedSharedPreferences (AES-256, Keystore-bound). */
class AndroidSecureStore(context: Context) : SecureStore {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "magnom.secure",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun readText(key: String): String? = prefs.getString(key, null)
    override fun writeText(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    override fun remove(key: String) { prefs.edit().remove(key).apply() }
}
