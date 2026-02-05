package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Repository responsible for collecting and transmitting anonymized usage data.
 *
 * This repository handles the submission of non-sensitive data points (like track lengths
 * and character sets) to a remote server for research and statistical analysis.
 * It ensures that all data is stripped of PII before transmission.
 *
 * @property settingsRepository Repository to check if data sharing is enabled.
 * @property client HTTP client for network requests.
 */
class AnalyticsRepository(
    private val settingsRepository: SettingsRepository,
    private val client: HttpClient
) {

    /**
     * Submits an anonymized card profile to the analytics server.
     *
     * @param cardProfile The full card profile to be anonymized and sent.
     */
    suspend fun anonymizeAndSendData(cardProfile: CardProfile) {
        if (!settingsRepository.isDataSharingEnabled()) return

        // Switch to IO dispatcher for network operations
        withContext(Dispatchers.IO) {
            // Create a safe, anonymized version of the card data
            val anonymizedProfile = AnonymizedCardProfile(
                track1Length = cardProfile.track1.length,
                track2Length = cardProfile.track2.length,
                // Extract unique characters to analyze encoding schemes without revealing data
                track1Charset = cardProfile.track1.toSet().joinToString(""),
                track2Charset = cardProfile.track2.toSet().joinToString(""),
                serviceCode = cardProfile.serviceCode
            )

            try {
                // Determine the endpoint URL based on the build type
                // Use localhost (via emulator's 10.0.2.2 alias) for debug builds
                val url = if (BuildConfig.DEBUG) "http://10.0.2.2:8080/data" else "https://analytics.hereliesaz.com/data"

                // Perform the POST request
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(anonymizedProfile)
                }
            } catch (e: Exception) {
                // Log failure but do not crash the app; analytics are optional
                Log.e("AnalyticsRepository", "Failed to submit data", e)
            }
        }
    }
}
