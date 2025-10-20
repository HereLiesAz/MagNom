package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.data.CardProfile
import com.hereliesaz.magnom.data.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val cardRepository = CardRepository(application)
    private val _cardProfiles = MutableStateFlow<List<CardProfile>>(emptyList())
    val cardProfiles: StateFlow<List<CardProfile>> = _cardProfiles

    fun loadCardProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _cardProfiles.value = cardRepository.getAllCardProfiles()
        }
    }
}
