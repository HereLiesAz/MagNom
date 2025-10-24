package com.hereliesaz.magnom.data

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
