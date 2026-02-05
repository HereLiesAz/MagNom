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
import kotlin.coroutines.coroutineContext

/**
 * UI State for the Bruteforce tool.
 *
 * @property target The target string to find (for simulation/testing purposes).
 * @property charset The character set to use for generating combinations.
 * @property currentAttempt The string currently being tested.
 * @property isRunning Whether the bruteforce process is active.
 */
data class BruteforceUiState(
    val target: String = "",
    val charset: String = "0123456789",
    val currentAttempt: String = "",
    val isRunning: Boolean = false,
)

/**
 * ViewModel for the Bruteforce Screen.
 *
 * Manages the logic for a simple recursive backtracking algorithm to iterate through
 * string combinations. This is primarily a demonstration or testing tool.
 */
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

    /**
     * Starts the bruteforce process.
     */
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

    /**
     * Recursively generates combinations of characters.
     *
     * @param prefix The current string built so far.
     * @param length The remaining length to generate.
     */
    private suspend fun generateCombinations(prefix: String, length: Int) {
        // Check for cancellation (stop button pressed)
        if (bruteforceJob?.isCancelled == true) return

        if (length == 0) {
            // Base case: simulation logic
            // We use a small delay to allow the UI to update and render the current attempt.
            // Without this, the loop would run too fast for the UI to observe.
            delay(1)
            _uiState.update { it.copy(currentAttempt = prefix) }

            if (prefix == _uiState.value.target) {
                stopBruteforce()
            }
            return
        }

        for (char in _uiState.value.charset) {
            // Check if coroutine is still active (not cancelled)
            if (!coroutineContext.isActive) break
            generateCombinations(prefix + char, length - 1)
        }
    }

    /**
     * Stops the running job.
     */
    fun stopBruteforce() {
        bruteforceJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }
}
