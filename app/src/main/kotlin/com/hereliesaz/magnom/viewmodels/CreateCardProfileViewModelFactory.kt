package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hereliesaz.magnom.audio.Swipe
import com.hereliesaz.magnom.data.CardRepository

class CreateCardProfileViewModelFactory(
    private val swipe: Swipe,
    private val cardRepository: CardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateCardProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateCardProfileViewModel(swipe, cardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
