package com.hereliesaz.magnom.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.AnalyticsRepository
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.data.ImageProcessingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI State for creating or editing a card profile.
 */
data class CreateCardProfileState(
    val name: String = "",
    val pan: String = "",
    val expirationDate: String = "",
    val serviceCode: String = "",
    val notes: List<String> = listOf(""),
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val error: String? = null
)

/**
 * ViewModel for the Create/Edit Card Profile screen.
 *
 * Handles user input, image processing (OCR) via [ImageProcessingRepository],
 * saving data to [CardRepository], and optional analytics reporting via [AnalyticsRepository].
 */
class CreateCardProfileViewModel(
    application: Application,
    private val cardId: String?,
    private val cardRepository: CardRepository,
    private val imageProcessingRepository: ImageProcessingRepository = ImageProcessingRepository(application),
    private val analyticsRepository: AnalyticsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateCardProfileState())
    val uiState: StateFlow<CreateCardProfileState> = _uiState

    init {
        cardId?.let {
            viewModelScope.launch {
                cardRepository.getCardProfile(it)?.let { profile ->
                    _uiState.value = _uiState.value.copy(
                        name = profile.name,
                        pan = profile.pan,
                        expirationDate = profile.expirationDate,
                        serviceCode = profile.serviceCode,
                        notes = profile.notes,
                        frontImageUri = profile.frontImageUri,
                        backImageUri = profile.backImageUri
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onPanChange(pan: String) {
        _uiState.value = _uiState.value.copy(pan = pan)
    }

    fun onExpirationDateChange(expirationDate: String) {
        _uiState.value = _uiState.value.copy(expirationDate = expirationDate)
    }

    fun onServiceCodeChange(serviceCode: String) {
        _uiState.value = _uiState.value.copy(serviceCode = serviceCode)
    }

    fun onNoteChange(index: Int, text: String) {
        val newNotes = _uiState.value.notes.toMutableList()
        newNotes[index] = text
        _uiState.value = _uiState.value.copy(notes = newNotes)
    }

    fun addNote() {
        _uiState.value = _uiState.value.copy(notes = _uiState.value.notes + "")
    }

    fun removeNote(index: Int) {
        val newNotes = _uiState.value.notes.toMutableList()
        newNotes.removeAt(index)
        _uiState.value = _uiState.value.copy(notes = newNotes)
    }

    /**
     * Processes the front image of the card.
     * Triggers OCR to auto-populate fields.
     */
    fun onFrontImageUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(frontImageUri = uri?.toString())
        uri?.let {
            viewModelScope.launch {
                val result = imageProcessingRepository.processImage(it)
                result.onSuccess { parsedData ->
                    parsedData.pan?.let { onPanChange(it) }
                    parsedData.expirationDate?.let { onExpirationDateChange(it) }
                    parsedData.name?.let { onNameChange(it) }
                }
                result.onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            }
        }
    }

    fun onBackImageUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(backImageUri = uri?.toString())
    }

    /**
     * Saves the card profile and sends anonymized data for analysis.
     */
    fun saveCardProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val cardProfile = CardProfile(
                id = cardId ?: UUID.randomUUID().toString(),
                name = currentState.name,
                pan = currentState.pan,
                expirationDate = currentState.expirationDate,
                serviceCode = currentState.serviceCode,
                notes = currentState.notes,
                frontImageUri = currentState.frontImageUri,
                backImageUri = currentState.backImageUri
            )
            cardRepository.saveCardProfile(cardProfile)
            // Use correct method name
            analyticsRepository.anonymizeAndSendData(cardProfile)
        }
    }
}
