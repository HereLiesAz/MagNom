package com.hereliesaz.magnom.domain

/** Metadata for a stored backup file. */
data class BackupInfo(val name: String, val sizeBytes: Long)

/**
 * Password-protected, portable export/import of all cards. Backups are encrypted with a
 * key derived from the user's password (independent of the device key), so a backup file
 * is only restorable with the same password. Files live in the app's own folder (deleted on
 * uninstall); there is currently no export or import UI to move them elsewhere.
 */
interface Backups {
    fun list(): List<BackupInfo>
    /** Create an encrypted backup of all cards. Returns the created file name. */
    suspend fun create(password: String): Result<String>
    /** Restore cards from a named backup. Returns the number of cards imported. */
    suspend fun restore(name: String, password: String): Result<Int>
    suspend fun delete(name: String)
}
