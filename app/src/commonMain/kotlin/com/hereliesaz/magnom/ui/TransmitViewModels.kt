package com.hereliesaz.magnom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.CardRepository
import com.hereliesaz.magnom.domain.SettingsRepository
import com.hereliesaz.magnom.domain.TransmitResult
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.domain.TransportKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransmitUiState(
    val card: Card? = null,
    val selected: TransportKind? = null,
    val busy: Boolean = false,
    val message: String? = null,
)

/**
 * Drives the single transmit screen for all transports. It regenerates nothing and trusts
 * nothing loose — it hands the whole [Card] to the chosen [Transmitter], whose contract
 * only accepts a Card, so empty track data can never be sent.
 */
class TransmitViewModel(
    private val transmitters: List<Transmitter>,
    private val repo: CardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TransmitUiState())
    val state: StateFlow<TransmitUiState> = _state.asStateFlow()

    val available: List<Transmitter> get() = transmitters.filter { it.isAvailable() }

    fun load(cardId: String) {
        val card = repo.get(cardId)
        _state.value = TransmitUiState(card = card, selected = available.firstOrNull()?.kind)
    }

    fun select(kind: TransportKind) = _state.update { it.copy(selected = kind, message = null) }

    fun transmit() {
        val s = _state.value
        val card = s.card ?: return
        val tx = available.firstOrNull { it.kind == s.selected } ?: return
        viewModelScope.launch {
            _state.value = s.copy(busy = true, message = "Transmitting over ${tx.kind}…")
            val result = tx.transmit(card)
            _state.value = _state.value.copy(
                busy = false,
                message = when (result) {
                    is TransmitResult.Success -> "Sent."
                    is TransmitResult.Failure -> "Failed: ${result.message}"
                },
            )
        }
    }

    override fun onCleared() { transmitters.forEach { it.release() } }

    private inline fun MutableStateFlow<TransmitUiState>.update(block: (TransmitUiState) -> TransmitUiState) {
        value = block(value)
    }
}

/** Settings: consent, app-lock, and transport availability. */
class SettingsViewModel(
    val settings: SettingsRepository,
    private val transmitters: List<Transmitter>,
) : ViewModel() {
    val appLockEnabled: StateFlow<Boolean> = settings.appLockEnabled
    val consentAccepted: StateFlow<Boolean> = settings.consentAccepted

    fun setAppLock(enabled: Boolean) = settings.setAppLockEnabled(enabled)
    fun setConsent(accepted: Boolean) = settings.setConsentAccepted(accepted)

    fun transports(): List<Pair<TransportKind, Boolean>> =
        transmitters.map { it.kind to it.isAvailable() }
}
