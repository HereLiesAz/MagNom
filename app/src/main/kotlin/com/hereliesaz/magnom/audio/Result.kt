package com.hereliesaz.magnom.audio

/**
 * A sealed class representing the outcome of an operation.
 *
 * Duplicate of [com.hereliesaz.magnom.utils.Result]. Used locally within the audio package.
 *
 * @param T The type of data returned on success.
 */
sealed class Result<out T> {
    /** Represents a successful operation. */
    data class Success<out T>(val data: T) : Result<T>()
    /** Represents a failed operation. */
    data class Error(val message: String) : Result<Nothing>()
}
