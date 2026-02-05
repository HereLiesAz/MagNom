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

/**
 * ViewModel for the Main Screen.
 *
 * Loads and exposes the list of saved card profiles.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Instantiate BackupManager and pass it to CardRepository to enable auto-backups
    private val cardRepository = CardRepository(application, BackupManager(application))
    private val _cardProfiles = MutableStateFlow<List<CardProfile>>(emptyList())
    val cardProfiles: StateFlow<List<CardProfile>> = _cardProfiles.asStateFlow()

    /**
     * Fetches all card profiles from the repository on a background thread.
     */
    fun loadCardProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _cardProfiles.value = cardRepository.getAllCardProfiles()
        }
    }
}
