package com.hereliesaz.magnom.transport

/**
 * Lets non-UI code (transmitters) ask the foreground activity for a runtime permission.
 * [MainActivity] installs [requester] while it exists; it is null otherwise, in which case a
 * request is simply reported as denied.
 */
object PermissionGate {
    @Volatile
    var requester: (suspend (permission: String) -> Boolean)? = null

    suspend fun request(permission: String): Boolean = requester?.invoke(permission) ?: false
}
