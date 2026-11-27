package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.Card
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopStoreTest {
    private fun dir() = Files.createTempDirectory("magnom-store").toFile()

    @Test fun round_trips_and_survives_reopen() {
        val d = dir()
        DesktopSecureStore(d).writeText("k", "v")
        assertEquals("v", DesktopSecureStore(d).readText("k"))
        assertTrue(d.listFiles()!!.none { it.name.endsWith(".tmp") }, "temp file left behind")
    }

    @Test fun unreadable_entry_is_preserved_not_destroyed() {
        val d = dir()
        DesktopSecureStore(d).writeText("k", "payload")
        // Lose the encryption key: a new store generates a different one.
        d.resolve("key").delete()
        val reopened = DesktopSecureStore(d)
        assertNull(reopened.readText("k"))
        assertTrue(d.resolve("store.properties").readText().contains("k.unreadable"))
    }

    @Test fun corrupt_card_store_is_kept_aside_and_not_overwritten() = runTest {
        val d = dir()
        val store = DesktopSecureStore(d)
        store.writeText("cards.v1", "{not json")
        val repo = JsonCardRepository(store, Json { ignoreUnknownKeys = true })
        assertTrue(repo.cards.value.isEmpty())
        assertNotNull(store.readText("cards.v1.corrupt"))
        val card = Card.fromFields("1", "x", "4111111111111111", "DOE/JOHN", "3012", "101").getOrThrow()
        repo.upsert(card)
        assertEquals("{not json", store.readText("cards.v1.corrupt"))
    }
}
