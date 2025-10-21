package com.hereliesaz.magnom.viewmodels

sealed class CardEditorError(val message: String) {
    object InvalidPan : CardEditorError("Invalid PAN")
    object InvalidExpirationDate : CardEditorError("Invalid expiration date")
    object InvalidServiceCode : CardEditorError("Invalid service code")
}
