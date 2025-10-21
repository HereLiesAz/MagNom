package com.hereliesaz.magnom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BruteforceUiState(
    val target: String = "",
    val charset: String = "0123456789",
    val currentAttempt: String = "",
    val isRunning: Boolean = false,
)

class BruteforceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BruteforceUiState())
    val uiState: StateFlow<BruteforceUiState> = _uiState.asStateFlow()

    private var bruteforceJob: Job? = null

    fun onTargetChange(target: String) {
        _uiState.update { it.copy(target = target) }
    }

    fun onCharsetChange(charset: String) {
        _uiState.update { it.copy(charset = charset) }
    }

    fun startBruteforce() {
        if (_uiState.value.target.isEmpty()) {
            return
        }
        _uiState.update { it.copy(isRunning = true) }
        bruteforceJob = viewModelScope.launch {
            generateCombinations("", _uiState.value.target.length)
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    private fun generateCombinations(prefix: String, length: Int) {
        if (bruteforceJob?.isCancelled == true) return
        if (length == 0) {
            viewModelScope.launch {
                if (!isActive) return@launch
                _uiState.update { it.copy(currentAttempt = prefix) }
                delay(1) // Allow UI to update
                if (prefix == _uiState.value.target) {
                    stopBruteforce()
                }
            }
            return
        }
        for (char in _uiState.value.charset) {
            generateCombinations(prefix + char, length - 1)
        }
    }

    fun stopBruteforce() {
        bruteforceJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }
}
