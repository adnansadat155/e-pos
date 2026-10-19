package com.dinevique.pos.ui.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Pass 86ing Item Availability Engine
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1 & REQ-01-POS FR-POS-003
 */

data class MenuItem86Event(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String = "MENU_ITEM_86",
    val itemId: String,
    val itemName: String,
    val isAvailable: Boolean,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

object Pass86Manager {

    private val _unavailableItemIds = MutableStateFlow<Set<String>>(emptySet())
    val unavailableItemIds: StateFlow<Set<String>> = _unavailableItemIds.asStateFlow()

    /**
     * Checks if an item is currently 86'd (unavailable)
     */
    fun isItem86d(itemId: String): Boolean {
        return _unavailableItemIds.value.contains(itemId)
    }

    /**
     * Toggles 86 status for an item and returns the event payload to broadcast over LAN
     */
    fun toggle86Item(itemId: String, itemName: String, isAvailable: Boolean): MenuItem86Event {
        _unavailableItemIds.update { set ->
            if (isAvailable) {
                set - itemId
            } else {
                set + itemId
            }
        }

        return MenuItem86Event(
            itemId = itemId,
            itemName = itemName,
            isAvailable = isAvailable
        )
    }

    /**
     * Ingests an incoming MENU_ITEM_86 event from a peer terminal over LAN
     */
    fun handleIncoming86Event(event: MenuItem86Event) {
        _unavailableItemIds.update { set ->
            if (event.isAvailable) {
                set - event.itemId
            } else {
                set + event.itemId
            }
        }
    }
}
