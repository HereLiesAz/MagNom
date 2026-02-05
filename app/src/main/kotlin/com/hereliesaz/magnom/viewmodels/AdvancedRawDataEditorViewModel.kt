package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.logic.ParsedTrack2Data
import com.hereliesaz.magnom.logic.TrackDataParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State for the Advanced Raw Data Editor screen.
 *
 * @property rawTrack1 The raw string input for Track 1.
 * @property rawTrack2 The raw string input for Track 2.
 * @property parsedTrack2Data The result of parsing the raw Track 2 string.
 * @property isTrack2Valid Boolean indicating if the current raw Track 2 is valid.
 */
data class AdvancedEditorState(
    val rawTrack1: String = "",
    val rawTrack2: String = "",
    val parsedTrack2Data: ParsedTrack2Data? = null,
    val isTrack2Valid: Boolean = true
)

/**
 * ViewModel for the Advanced Raw Data Editor.
 *
 * Manages the state of the raw text fields and performs real-time validation
 * and parsing of the input data using [TrackDataParser].
 */
class AdvancedRawDataEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdvancedEditorState())
    val uiState: StateFlow<AdvancedEditorState> = _uiState.asStateFlow()

    private val trackDataParser = TrackDataParser()

    /**
     * Updates the raw Track 1 data.
     */
    fun onTrack1Change(newTrack1: String) {
        _uiState.value = _uiState.value.copy(rawTrack1 = newTrack1)
    }

    /**
     * Updates the raw Track 2 data and attempts to parse it.
     *
     * Updates [AdvancedEditorState.parsedTrack2Data] and [AdvancedEditorState.isTrack2Valid].
     */
    fun onTrack2Change(newTrack2: String) {
        val parsedData = trackDataParser.parseTrack2(newTrack2)
        _uiState.value = _uiState.value.copy(
            rawTrack2 = newTrack2,
            parsedTrack2Data = parsedData,
            isTrack2Valid = parsedData != null || newTrack2.isEmpty()
        )
    }
}
