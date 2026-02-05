package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.BuildConfig
import com.hereliesaz.magnom.logic.TrackDataGenerator
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

        // Switch to IO dispatcher for network operations and heavy generation logic
        withContext(Dispatchers.IO) {

            // Generate full track strings to ensure accurate length and charset analysis.
            // The stored profile might have empty 'track1'/'track2' fields if they haven't been
            // manually generated or edited yet. The generator creates the standard ISO formats
            // including sentinels and LRCs.
            val trackDataGenerator = TrackDataGenerator()
            val generatedTrack1 = try {
                trackDataGenerator.generateTrack1(
                    cardProfile.pan,
                    cardProfile.name,
                    cardProfile.expirationDate,
                    cardProfile.serviceCode
                )
            } catch (e: Exception) { "" } // Fallback to empty if generation fails (e.g. invalid input)

            val generatedTrack2 = try {
                trackDataGenerator.generateTrack2(
                    cardProfile.pan,
                    cardProfile.expirationDate,
                    cardProfile.serviceCode
                )
            } catch (e: Exception) { "" }

            // Create a safe, anonymized version of the card data
            val anonymizedProfile = AnonymizedCardProfile(
                track1Length = generatedTrack1.length,
                track2Length = generatedTrack2.length,
                // Extract unique characters to analyze encoding schemes without revealing data
                track1Charset = generatedTrack1.toSet().joinToString(""),
                track2Charset = generatedTrack2.toSet().joinToString(""),
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
