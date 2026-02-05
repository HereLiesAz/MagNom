package com.hereliesaz.magnom.data

import kotlinx.serialization.Serializable

/**
 * A safe, stripped-down version of [CardProfile] used for analytics.
 *
 * This class contains only structural metadata about the card's data,
 * ensuring that no Personally Identifiable Information (PII) is transmitted.
 *
 * @property track1Length The length of the Track 1 data string.
 * @property track2Length The length of the Track 2 data string.
 * @property track1Charset The set of unique characters present in Track 1 (for encoding analysis).
 * @property track2Charset The set of unique characters present in Track 2.
 * @property serviceCode The service code, which indicates card technology (magstripe vs chip) and interchange rules.
 */
@Serializable
data class AnonymizedCardProfile(
    val track1Length: Int,
    val track2Length: Int,
    val track1Charset: String,
    val track2Charset: String,
    val serviceCode: String
)
