package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AdvancedEditorViewModel(
    private val cardRepository: CardRepository,
    private val cardId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CardProfile(
            id = "",
            name = "",
            pan = "",
            expirationDate = "",
            serviceCode = ""
        )
    )
    val uiState: StateFlow<CardProfile> = _uiState.asStateFlow()

    init {
        if (cardId != null) {
            viewModelScope.launch {
                val cardProfile = cardRepository.getCardProfile(cardId)
                if (cardProfile != null) {
                    _uiState.value = cardProfile
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updatePan(pan: String) {
        _uiState.value = _uiState.value.copy(pan = pan)
    }

    fun updateExpirationDate(expirationDate: String) {
        _uiState.value = _uiState.value.copy(expirationDate = expirationDate)
    }

    fun updateTrack1(track1: String) {
        _uiState.value = _uiState.value.copy(track1 = track1)
    }

    fun updateTrack2(track2: String) {
        _uiState.value = _uiState.value.copy(track2 = track2)
    }

    fun save() {
        viewModelScope.launch {
            val cardProfile = if (cardId == null) {
                _uiState.value.copy(id = UUID.randomUUID().toString())
            } else {
                _uiState.value
            }
            cardRepository.saveCardProfile(cardProfile)
        }
    }
}

class AdvancedEditorViewModelFactory(
    private val cardRepository: CardRepository,
    private val cardId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdvancedEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdvancedEditorViewModel(cardRepository, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
