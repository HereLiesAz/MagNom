package com.hereliesaz.magnom.data

data class CardProfile(
    val id: String,
    val name: String,
    val pan: String,
    val expirationDate: String,
    val serviceCode: String,
    val notes: String = "",
    val frontImageUri: String? = null,
    val backImageUri: String? = null
)
