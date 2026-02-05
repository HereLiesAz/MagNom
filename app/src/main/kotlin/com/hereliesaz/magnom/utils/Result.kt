package com.hereliesaz.magnom.utils

/**
 * A sealed class representing the outcome of an operation.
 *
 * This pattern is used throughout the application to handle success and failure
 * states explicitly, avoiding the need for exception handling in UI logic.
 *
 * @param T The type of data returned on success.
 */
sealed class Result<out T> {

    /**
     * Represents a successful operation.
     *
     * @property data The result data.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation.
     *
     * @property message A descriptive error message.
     */
    data class Error(val message: String) : Result<Nothing>()
}
