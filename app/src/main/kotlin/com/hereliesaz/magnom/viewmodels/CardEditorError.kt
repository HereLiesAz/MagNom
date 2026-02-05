package com.hereliesaz.magnom.viewmodels

/**
 * Sealed class representing possible validation errors in the Card Editor.
 */
sealed class CardEditorError(val message: String) {
    /** The PAN is invalid (wrong length or format). */
    object InvalidPan : CardEditorError("Invalid PAN")
    /** The expiration date is invalid (wrong format or date). */
    object InvalidExpirationDate : CardEditorError("Invalid expiration date")
    /** The service code is invalid (must be 3 digits). */
    object InvalidServiceCode : CardEditorError("Invalid service code")
}
