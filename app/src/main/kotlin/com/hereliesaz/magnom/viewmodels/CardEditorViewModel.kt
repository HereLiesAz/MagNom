package com.hereliesaz.magnom.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.data.ImageProcessingRepository
import com.hereliesaz.magnom.utils.TextParsing
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

sealed class NavigationEvent {
    data class ToUrl(val url: String) : NavigationEvent()
}

data class CardEditorState(
    val id: String? = null,
    val name: String = "",
    val pan: String = "",
    val expirationDate: String = "",
    val serviceCode: String = "",
    val notes: String = "",
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val error: CardEditorError? = null
)

class CardEditorViewModel(application: Application, private val cardId: String? = null) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application, BackupManager(application))
    private val imageProcessingRepository = ImageProcessingRepository(application)
    private val _uiState = MutableStateFlow(CardEditorState())
    val uiState: StateFlow<CardEditorState> = _uiState.asStateFlow()
    private var saveJob: Job? = null
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        cardId?.let {
            viewModelScope.launch {
                cardRepository.getCardProfile(it)?.let { profile ->
                    _uiState.value = _uiState.value.copy(
                        id = profile.id,
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
        debouncedSave()
    }

    fun onPanChange(pan: String) {
        if (pan.length > 19 || !pan.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(error = CardEditorError.InvalidPan)
        } else {
            _uiState.value = _uiState.value.copy(pan = pan, error = null)
            debouncedSave()
        }
    }

    fun onExpirationDateChange(expirationDate: String) {
        if (expirationDate.length != 4 || !expirationDate.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(error = CardEditorError.InvalidExpirationDate)
        } else {
            _uiState.value = _uiState.value.copy(expirationDate = expirationDate, error = null)
            debouncedSave()
        }
    }

    fun onServiceCodeChange(serviceCode: String) {
        if (serviceCode.length != 3 || !serviceCode.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(error = CardEditorError.InvalidServiceCode)
        } else {
            _uiState.value = _uiState.value.copy(serviceCode = serviceCode, error = null)
            debouncedSave()
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
        debouncedSave()
    }

    fun onFrontImageUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(frontImageUri = uri?.toString())
        debouncedSave()
        uri?.let {
            viewModelScope.launch {
                val result = imageProcessingRepository.processImage(it)
                result.onSuccess { parsedData ->
                    parsedData.pan?.let { onPanChange(it) }
                    parsedData.expirationDate?.let { onExpirationDateChange(it) }
                    parsedData.name?.let { onNameChange(it) }
                }
                result.onFailure {
                    // This error is not a validation error, so we'll just log it for now
                }
            }
        }
    }

    fun onBackImageUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(backImageUri = uri?.toString())
        debouncedSave()
    }

    fun saveCardProfile() {
        if (_uiState.value.error != null) return
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val newProfile = CardProfile(
                id = currentState.id ?: UUID.randomUUID().toString(),
                name = currentState.name,
                pan = currentState.pan,
                expirationDate = currentState.expirationDate,
                serviceCode = currentState.serviceCode,
                notes = currentState.notes,
                frontImageUri = currentState.frontImageUri,
                backImageUri = currentState.backImageUri
            )
            cardRepository.saveCardProfile(newProfile)
            if (currentState.id == null) {
                _uiState.value = _uiState.value.copy(id = newProfile.id)
            }
        }
    }

    private fun debouncedSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            saveCardProfile()
        }
    }

    fun smartBackgroundCheck(name: String) {
        val names = name.split(" ")
        val firstName = names.getOrNull(0) ?: ""
        val lastName = names.getOrNull(1) ?: ""
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.ToUrl("https://smartbackgroundchecks.com/search?firstName=$firstName&lastName=$lastName"))
        }
    }

    fun geminiDeepResearch() {
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.ToUrl("https://gemini.google.com"))
        }
    }
}
