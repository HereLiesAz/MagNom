package com.hereliesaz.magnom.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for handling audio file selection.
 *
 * @property selectedFileUri StateFlow holding the URI of the currently selected audio file.
 */
class AudioViewModel : ViewModel() {

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri

    /**
     * Updates the selected file URI.
     */
    fun onFileSelected(uri: Uri) {
        _selectedFileUri.value = uri
    }
}
