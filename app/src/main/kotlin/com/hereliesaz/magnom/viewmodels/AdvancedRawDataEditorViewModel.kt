package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import com.hereliesaz.magnom.logic.ParsedTrack2Data
import com.hereliesaz.magnom.logic.TrackDataParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdvancedEditorState(
    val rawTrack1: String = "",
    val rawTrack2: String = "",
    val parsedTrack2Data: ParsedTrack2Data? = null,
    val isTrack2Valid: Boolean = true
)

class AdvancedRawDataEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdvancedEditorState())
    val uiState: StateFlow<AdvancedEditorState> = _uiState.asStateFlow()

    private val trackDataParser = TrackDataParser()

    fun onTrack1Change(newTrack1: String) {
        _uiState.value = _uiState.value.copy(rawTrack1 = newTrack1)
    }

    fun onTrack2Change(newTrack2: String) {
        val parsedData = trackDataParser.parseTrack2(newTrack2)
        _uiState.value = _uiState.value.copy(
            rawTrack2 = newTrack2,
            parsedTrack2Data = parsedData,
            isTrack2Valid = parsedData != null || newTrack2.isEmpty()
        )
    }
}
