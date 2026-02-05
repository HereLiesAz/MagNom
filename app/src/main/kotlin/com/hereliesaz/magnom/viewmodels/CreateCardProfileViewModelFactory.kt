package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hereliesaz.magnom.data.AnalyticsRepository
import com.hereliesaz.magnom.data.BackupManager
import com.hereliesaz.magnom.data.CardRepository
import com.hereliesaz.magnom.data.ImageProcessingRepository
import com.hereliesaz.magnom.data.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating [CreateCardProfileViewModel] instances.
 *
 * Handles dependency injection manually.
 */
class CreateCardProfileViewModelFactory(
    private val application: Application,
    private val cardId: String?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateCardProfileViewModel::class.java)) {
            val settingsRepository = SettingsRepository(application)
            // Use BackupManager defaults
            val cardRepository = cardId?.let {
                CardRepository(application)
            } ?: CardRepository(application)

            val imageProcessingRepository = ImageProcessingRepository(application)

            val ktorClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                    })
                }
            }
            // Pass required dependencies to AnalyticsRepository
            val analyticsRepository = AnalyticsRepository(settingsRepository, ktorClient)

            @Suppress("UNCHECKED_CAST")
            return CreateCardProfileViewModel(
                application,
                cardId,
                cardRepository,
                imageProcessingRepository,
                analyticsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
