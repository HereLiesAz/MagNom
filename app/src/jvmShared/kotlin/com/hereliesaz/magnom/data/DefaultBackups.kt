package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.BackupInfo
import com.hereliesaz.magnom.domain.Backups
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * File-based, password-protected backups shared by Android and desktop. Each backup is an
 * encrypted blob ([BackupCrypto]) holding the serialized card list. Restored cards go back
 * through [CardRepository.upsert], so the Card invariant still holds on import.
 */
class DefaultBackups(
    private val repo: CardRepository,
    private val json: Json,
    private val baseDir: File,
) : Backups {

    init { runCatching { baseDir.mkdirs() } }

    override fun list(): List<BackupInfo> =
        baseDir.listFiles { f -> f.isFile && f.name.endsWith(".$EXT") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { BackupInfo(it.name, it.length()) }
            ?: emptyList()

    override suspend fun create(password: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(password.length >= MIN_PASSWORD) { "Password must be at least $MIN_PASSWORD characters" }
            val cards = repo.cards.value
            val plain = json.encodeToString(ListSerializer(Card.serializer()), cards).encodeToByteArray()
            val blob = BackupCrypto.encrypt(plain, password.toCharArray())
            val file = File(baseDir, "magnom-${System.currentTimeMillis()}.$EXT")
            file.writeBytes(blob)
            file.name
        }
    }

    override suspend fun restore(name: String, password: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(baseDir, name)
            require(file.exists()) { "Backup not found" }
            val plain = BackupCrypto.decrypt(file.readBytes(), password.toCharArray())
            val cards = json.decodeFromString(ListSerializer(Card.serializer()), plain.decodeToString())
            cards.forEach { repo.upsert(it) }
            cards.size
        }
    }

    override suspend fun delete(name: String) {
        withContext(Dispatchers.IO) { runCatching { File(baseDir, name).delete() } }
    }

    companion object {
        const val EXT = "mnb"
        const val MIN_PASSWORD = 8
    }
}
