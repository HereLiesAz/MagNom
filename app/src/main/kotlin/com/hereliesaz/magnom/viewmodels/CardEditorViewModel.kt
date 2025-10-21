package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CardEditorState(
    val name: String = "",
    val pan: String = "",
    val expirationDate: String = "",
    val serviceCode: String = ""
)

class CardEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application, BackupManager(application))
    private val _uiState = MutableStateFlow(CardEditorState())
    val uiState: StateFlow<CardEditorState> = _uiState.asStateFlow()

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

    fun saveCardProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val newProfile = CardProfile(
                id = UUID.randomUUID().toString(),
                name = currentState.name,
                pan = currentState.pan,
                expirationDate = currentState.expirationDate,
                serviceCode = currentState.serviceCode
            )
            cardRepository.saveCardProfile(newProfile)
        }
    }
}
