package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WaveformViewModelFactory(private val application: Application, private val cardId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaveformViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaveformViewModel(application, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
