package com.hereliesaz.magnom.data

import android.util.Log
import com.hereliesaz.magnom.logic.TrackDataGenerator
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsRepository(
    private val settingsRepository: SettingsRepository,
    private val client: HttpClient
) {

    private val trackDataGenerator = TrackDataGenerator()

    fun anonymizeAndSendData(profile: CardProfile) {
        if (!settingsRepository.isDataSharingEnabled()) {
            return
        }

        val track1 = trackDataGenerator.generateTrack1(profile.pan, profile.name, profile.expirationDate, profile.serviceCode)
        val track2 = trackDataGenerator.generateTrack2(profile.pan, profile.expirationDate, profile.serviceCode)

        val anonymizedProfile = AnonymizedCardProfile(
            track1Length = track1.length,
            track2Length = track2.length,
            track1Charset = track1.map { it }.distinct().joinToString(""),
            track2Charset = track2.map { it }.distinct().joinToString(""),
            serviceCode = profile.serviceCode
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.post("https://placeholder.analytics.endpoint/data") {
                    contentType(ContentType.Application.Json)
                    setBody(anonymizedProfile)
                }
            } catch (e: Exception) {
                Log.e("AnalyticsRepository", "Failed to send analytics data", e)
            }
        }
    }
}
