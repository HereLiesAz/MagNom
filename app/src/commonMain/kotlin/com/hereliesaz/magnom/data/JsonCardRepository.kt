package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.CardRepository
import com.hereliesaz.magnom.domain.SecureStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Card persistence backed by an encrypted [SecureStore] and kotlinx.serialization.
 *
 * Because [Card] instances can only be produced through its validating factories, every
 * record written here already carries valid Track 1/Track 2 data — the store never has to
 * defend against, or silently accept, a card with empty tracks.
 */
class JsonCardRepository(
    private val store: SecureStore,
    private val json: Json,
) : CardRepository {

    private val _cards = MutableStateFlow(load())
    override val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    override fun get(id: String): Card? = _cards.value.firstOrNull { it.id == id }

    override suspend fun upsert(card: Card) {
        val byId = _cards.value.associateBy { it.id }.toMutableMap()
        byId[card.id] = card
        persist(byId.values.sortedBy { it.label.lowercase() })
    }

    override suspend fun delete(id: String) {
        persist(_cards.value.filterNot { it.id == id })
    }

    private fun persist(list: List<Card>) {
        store.writeText(KEY, json.encodeToString(ListSerializer(Card.serializer()), list))
        _cards.value = list
    }

    /**
     * Loads stored cards. If the stored text can't be parsed, it is copied to [KEY_CORRUPT]
     * before anything can overwrite it, so a bad read never turns into silent data loss.
     * Cards that fail [Card.isConsistent] are dropped rather than trusted.
     */
    private fun load(): List<Card> {
        val text = store.readText(KEY) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(Card.serializer()), text) }
            .map { list -> list.filter { it.isConsistent() } }
            .getOrElse {
                if (store.readText(KEY_CORRUPT) == null) store.writeText(KEY_CORRUPT, text)
                emptyList()
            }
    }

    companion object {
        private const val KEY = "cards.v1"
        private const val KEY_CORRUPT = "cards.v1.corrupt"
    }
}
