package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardRepository

class ParseViewModelFactory(
    private val application: Application,
    private val cardId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParseViewModel(CardRepository(application, BackupManager(application)), cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
