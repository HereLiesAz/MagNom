package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.Swipe
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CreateCardProfileViewModel(
    private val swipe: Swipe,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _pan = MutableStateFlow("")
    val pan: StateFlow<String> = _pan

    private val _expirationDate = MutableStateFlow("")
    val expirationDate: StateFlow<String> = _expirationDate

    private val _serviceCode = MutableStateFlow("")
    val serviceCode: StateFlow<String> = _serviceCode

    fun onNameChange(name: String) {
        _name.value = name
    }

    fun onPanChange(pan: String) {
        _pan.value = pan
    }

    fun onExpirationDateChange(expirationDate: String) {
        _expirationDate.value = expirationDate
    }

    fun onServiceCodeChange(serviceCode: String) {
        _serviceCode.value = serviceCode
    }

    fun saveCardProfile() {
        viewModelScope.launch {
            val cardProfile = CardProfile(
                id = UUID.randomUUID().toString(),
                name = name.value,
                pan = pan.value,
                expirationDate = expirationDate.value,
                serviceCode = serviceCode.value
            )
            cardRepository.saveCardProfile(cardProfile)
        }
    }
}
