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

    private fun load(): List<Card> =
        store.readText(KEY)?.let {
            runCatching { json.decodeFromString(ListSerializer(Card.serializer()), it) }.getOrDefault(emptyList())
        } ?: emptyList()

    companion object {
        private const val KEY = "cards.v1"
    }
}
