package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CardEditorViewModelFactory(
    private val application: Application,
    private val cardId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardEditorViewModel(application, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
