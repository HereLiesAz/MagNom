package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hereliesaz.magnom.data.CardRepository

class WaveformViewModelFactory(private val cardRepository: CardRepository, private val cardId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaveformViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaveformViewModel(cardRepository, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
