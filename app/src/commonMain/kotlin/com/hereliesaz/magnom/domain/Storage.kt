package com.hereliesaz.magnom.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * A small key/value blob store that keeps its values encrypted at rest. Each platform
 * supplies a real implementation — Android via EncryptedSharedPreferences, desktop via an
 * AES-GCM file keyed from the OS user — so no card data is ever written in plaintext.
 */
interface SecureStore {
    fun readText(key: String): String?
    fun writeText(key: String, value: String)
    fun remove(key: String)
}

/** Persists [Card] profiles. The list is observable so the UI updates reactively. */
interface CardRepository {
    val cards: StateFlow<List<Card>>
    fun get(id: String): Card?
    suspend fun upsert(card: Card)
    suspend fun delete(id: String)
}

/** Local, on-device settings. Nothing here ever leaves the device. */
interface SettingsRepository {
    val consentAccepted: StateFlow<Boolean>
    val appLockEnabled: StateFlow<Boolean>
    fun setConsentAccepted(value: Boolean)
    fun setAppLockEnabled(value: Boolean)
}
