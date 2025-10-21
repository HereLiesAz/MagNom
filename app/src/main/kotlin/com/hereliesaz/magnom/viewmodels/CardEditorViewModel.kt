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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

data class CardEditorState(
    val id: String? = null,
    val name: String = "",
    val pan: String = "",
    val expirationDate: String = "",
    val serviceCode: String = "",
    val notes: String = "",
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val error: String? = null
)

class CardEditorViewModel(application: Application, private val cardId: String? = null) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application, BackupManager(application))
    private val imageProcessingRepository = ImageProcessingRepository(application)
    private val _uiState = MutableStateFlow(CardEditorState())
    val uiState: StateFlow<CardEditorState> = _uiState.asStateFlow()
    private var saveJob: Job? = null

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
        _uiState.value = _uiState.value.copy(pan = pan)
        debouncedSave()
    }

    fun onExpirationDateChange(expirationDate: String) {
        _uiState.value = _uiState.value.copy(expirationDate = expirationDate)
        debouncedSave()
    }

    fun onServiceCodeChange(serviceCode: String) {
        _uiState.value = _uiState.value.copy(serviceCode = serviceCode)
        debouncedSave()
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
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            }
        }
    }

    fun onBackImageUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(backImageUri = uri?.toString())
        debouncedSave()
    }

    fun saveCardProfile() {
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

    fun smartBackgroundCheck(context: Context, name: String) {
        val names = name.split(" ")
        val firstName = names.getOrNull(0) ?: ""
        val lastName = names.getOrNull(1) ?: ""
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://smartbackgroundchecks.com/search?firstName=$firstName&lastName=$lastName")
        }
        context.startActivity(intent)
    }

    fun geminiDeepResearch(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://gemini.google.com")
        }
        context.startActivity(intent)
    }
}
