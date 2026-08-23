package com.hereliesaz.magnom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.core.Lrc
import com.hereliesaz.magnom.core.TrackCodec
import com.hereliesaz.magnom.core.TrackFormat
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.CardRepository
import com.hereliesaz.magnom.domain.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The card list / home screen. */
class CardListViewModel(private val repo: CardRepository) : ViewModel() {
    val cards: StateFlow<List<Card>> = repo.cards
    fun delete(id: String) { viewModelScope.launch { repo.delete(id) } }
}

/** Editable field state for the field-based editor. */
data class EditorUiState(
    val id: String = "",
    val label: String = "",
    val pan: String = "",
    val name: String = "",
    val expiration: String = "",
    val serviceCode: String = "",
    val notes: List<String> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
)

/** Create or edit a card from human-readable fields. */
class CardEditorViewModel(private val repo: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun load(id: String?) {
        val existing = id?.let { repo.get(it) }
        _state.value = if (existing != null) {
            EditorUiState(existing.id, existing.label, existing.pan, existing.name, existing.expiration, existing.serviceCode, existing.notes)
        } else {
            EditorUiState(id = newId())
        }
    }

    fun onLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun onPan(v: String) = _state.update { it.copy(pan = v.filter { c -> c.isDigit() }, error = null) }
    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onExpiration(v: String) = _state.update { it.copy(expiration = v.filter { c -> c.isDigit() }.take(4), error = null) }
    fun onServiceCode(v: String) = _state.update { it.copy(serviceCode = v.filter { c -> c.isDigit() }.take(3), error = null) }
    fun addNote(text: String) = _state.update { it.copy(notes = it.notes + text) }
    fun removeNote(index: Int) = _state.update { it.copy(notes = it.notes.filterIndexed { i, _ -> i != index }) }

    fun save() {
        val s = _state.value
        Card.fromFields(s.id.ifBlank { newId() }, s.label, s.pan, s.name, s.expiration, s.serviceCode, s.notes).fold(
            onSuccess = { card ->
                viewModelScope.launch { repo.upsert(card) }
                _state.update { it.copy(saved = true, error = null) }
            },
            onFailure = { e -> _state.update { it.copy(error = e.message ?: "Could not build card") } },
        )
    }
}

/** Live-validating raw Track 1 / Track 2 editor and analyzer. */
data class RawUiState(
    val id: String = "",
    val label: String = "",
    val track1: String = "",
    val track2: String = "",
    val track1Valid: Boolean = false,
    val track2Valid: Boolean = false,
    val decoded: String? = null,
    val error: String? = null,
    val saved: Boolean = false,
)

class RawAnalyzerViewModel(private val repo: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(RawUiState(id = newId()))
    val state: StateFlow<RawUiState> = _state.asStateFlow()

    fun onTrack1(v: String) = _state.update { it.copy(track1 = v, track1Valid = Lrc.validate(v, TrackFormat.TRACK1), error = null).withDecoded() }
    fun onTrack2(v: String) = _state.update { it.copy(track2 = v, track2Valid = Lrc.validate(v, TrackFormat.TRACK2), error = null).withDecoded() }
    fun onLabel(v: String) = _state.update { it.copy(label = v) }

    private fun RawUiState.withDecoded(): RawUiState {
        val t2 = TrackCodec.parseTrack2(track2)
        val decoded = t2?.let { "PAN ${it.pan}  EXP ${it.expiration}  SVC ${it.serviceCode}" }
        return copy(decoded = decoded)
    }

    fun save() {
        val s = _state.value
        Card.fromRawTracks(s.id.ifBlank { newId() }, s.label, s.track1, s.track2, emptyList()).fold(
            onSuccess = { card ->
                viewModelScope.launch { repo.upsert(card) }
                _state.update { it.copy(saved = true, error = null) }
            },
            onFailure = { e -> _state.update { it.copy(error = e.message ?: "Tracks invalid") } },
        )
    }
}
