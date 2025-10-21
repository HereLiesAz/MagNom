package com.hereliesaz.magnom.data

import android.util.Log
import com.hereliesaz.magnom.logic.TrackDataGenerator

class AnalyticsRepository {

    private val trackDataGenerator = TrackDataGenerator()

    fun anonymizeAndSendData(profile: CardProfile) {
        val track1 = trackDataGenerator.generateTrack1(profile.pan, profile.name, profile.expirationDate, profile.serviceCode)
        val track2 = trackDataGenerator.generateTrack2(profile.pan, profile.expirationDate, profile.serviceCode)

        val anonymizedProfile = AnonymizedCardProfile(
            track1Length = track1.length,
            track2Length = track2.length,
            track1Charset = track1.map { it }.distinct().joinToString(""),
            track2Charset = track2.map { it }.distinct().joinToString(""),
            serviceCode = profile.serviceCode
        )

        // TODO: Replace this with a real network call to your analytics service
        Log.d("AnalyticsRepository", "Sending anonymized data: $anonymizedProfile")
    }
}
