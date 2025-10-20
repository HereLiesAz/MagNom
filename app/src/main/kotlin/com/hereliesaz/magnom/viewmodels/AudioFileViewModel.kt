package com.hereliesaz.magnom.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.magnom.audio.AudioParser
import com.hereliesaz.magnom.audio.Swipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioFileViewModel : ViewModel() {

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri

    private val _swipes = MutableStateFlow<List<Swipe>>(emptyList())
    val swipes: StateFlow<List<Swipe>> = _swipes

    private val _selectedSwipe = MutableStateFlow<Swipe?>(null)
    val selectedSwipe: StateFlow<Swipe?> = _selectedSwipe

    fun onFileSelected(context: Context, uri: Uri) {
        _selectedFileUri.value = uri
        viewModelScope.launch {
            val parser = AudioParser(context, uri)
            _swipes.value = parser.parse()
        }
    }

    fun onSwipeSelected(swipe: Swipe) {
        _selectedSwipe.value = swipe
    }
}
