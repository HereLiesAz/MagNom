package com.hereliesaz.magnom.data

/**
 * Data class representing a magnetic stripe card profile.
 *
 * This class serves as the core data model for the application, storing all necessary
 * information to reconstruct and emulate a magnetic stripe card. It includes both
 * the parsed, human-readable fields (PAN, Name) and the raw track data.
 *
 * @property id Unique identifier for the card profile (UUID string).
 * @property name User-assigned name for the profile (e.g., "My Visa").
 * @property pan Primary Account Number (the card number).
 * @property expirationDate Expiration date in MM/YY format.
 * @property serviceCode The 3-digit service code (e.g., 101).
 * @property track1 The raw Track 1 data string (IATA format).
 * @property track2 The raw Track 2 data string (ABA format).
 * @property notes A list of user-defined notes associated with the card.
 * @property frontImageUri URI string pointing to the stored image of the card front.
 * @property backImageUri URI string pointing to the stored image of the card back.
 */
data class CardProfile(
    val id: String,
    val name: String,
    val pan: String,
    val expirationDate: String,
    val serviceCode: String,
    val track1: String = "",
    val track2: String = "",
    val notes: List<String> = emptyList(),
    val frontImageUri: String? = null,
    val backImageUri: String? = null
)
