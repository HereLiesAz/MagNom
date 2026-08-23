package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.SecureStore
import com.hereliesaz.magnom.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** On-device settings persisted in the encrypted [SecureStore]. */
class DefaultSettingsRepository(private val store: SecureStore) : SettingsRepository {

    private val _consent = MutableStateFlow(store.readText(CONSENT) == "1")
    override val consentAccepted: StateFlow<Boolean> = _consent.asStateFlow()

    private val _appLock = MutableStateFlow(store.readText(APP_LOCK) == "1")
    override val appLockEnabled: StateFlow<Boolean> = _appLock.asStateFlow()

    override fun setConsentAccepted(value: Boolean) {
        store.writeText(CONSENT, if (value) "1" else "0")
        _consent.value = value
    }

    override fun setAppLockEnabled(value: Boolean) {
        store.writeText(APP_LOCK, if (value) "1" else "0")
        _appLock.value = value
    }

    companion object {
        private const val CONSENT = "consent.accepted"
        private const val APP_LOCK = "applock.enabled"
    }
}
