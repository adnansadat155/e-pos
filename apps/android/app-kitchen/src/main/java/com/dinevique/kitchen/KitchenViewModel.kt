package com.dinevique.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinevique.core.network.LanWebSocketClient
import com.dinevique.core.network.models.LanBumpPayload
import com.dinevique.core.network.models.LanEventEnvelope
import com.dinevique.core.network.models.LanTicketPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

data class UiTicket(
    val ticketNumber: String,
    val orderUuid: String,
    val tableLabel: String,
    val station: String = "EXPO", // 'GRILL', 'FRY', 'SALAD', 'BAR', 'EXPO'
    val statusLabel: String, // 'NEW_TICKET', 'IN_PROGRESS', 'READY', 'BUMPED'
    val items: List<String>,
    val createdAtEpochMs: Long
) {
    fun formattedElapsed(currentEpochMs: Long): String {
        val diffMs = (currentEpochMs - createdAtEpochMs).coerceAtLeast(0)
        val mins = (diffMs / 1000) / 60
        val secs = (diffMs / 1000) % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

class KitchenViewModel(
    private val client: LanWebSocketClient = LanWebSocketClient()
) : ViewModel() {

    private val _tickets = MutableStateFlow<List<UiTicket>>(emptyList())
    val tickets: StateFlow<List<UiTicket>> = _tickets.asStateFlow()

    private val _selectedStation = MutableStateFlow("ALL")
    val selectedStation: StateFlow<String> = _selectedStation.asStateFlow()

    private val _connectionState = MutableStateFlow("DISCONNECTED")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _currentEpochMs = MutableStateFlow(System.currentTimeMillis())
    val currentEpochMs: StateFlow<Long> = _currentEpochMs.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadMockInitialTickets()
        startTimerTicker()
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentEpochMs.value = System.currentTimeMillis()
            }
        }
    }

    private fun loadMockInitialTickets() {
        val now = System.currentTimeMillis()
        _tickets.value = listOf(
            UiTicket(
                ticketNumber = "#104",
                orderUuid = "ord-104",
                tableLabel = "Table 2",
                station = "GRILL",
                statusLabel = "NEW_TICKET",
                items = listOf("1x USDA Prime Ribeye 14oz (Medium Rare)", "1x Truffle Parmesan Fries"),
                createdAtEpochMs = now - 4 * 60 * 1000
            ),
            UiTicket(
                ticketNumber = "#105",
                orderUuid = "ord-105",
                tableLabel = "Table 7",
                station = "FRY",
                statusLabel = "IN_PROGRESS",
                items = listOf("2x Crispy Point Judith Calamari"),
                createdAtEpochMs = now - 11 * 60 * 1000
            ),
            UiTicket(
                ticketNumber = "#106",
                orderUuid = "ord-106",
                tableLabel = "Bar 1",
                station = "BAR",
                statusLabel = "READY",
                items = listOf("1x Smoked Bourbon Old Fashioned"),
                createdAtEpochMs = now - 18 * 60 * 1000
            )
        )
    }

    fun selectStation(station: String) {
        _selectedStation.value = station
    }

    fun connectToMaster(host: String, port: Int = 8080) {
        viewModelScope.launch {
            try {
                _connectionState.value = "CONNECTING"
                client.connect(host, port)
                _connectionState.value = "CONNECTED"

                client.incomingMessages.collect { rawText ->
                    handleIncomingFrame(rawText)
                }
            } catch (e: Exception) {
                _connectionState.value = "ERROR: ${e.message}"
            }
        }
    }

    private fun handleIncomingFrame(rawText: String) {
        try {
            val envelope = json.decodeFromString<LanEventEnvelope>(rawText)
            when (envelope.eventType) {
                "TICKET_RECEIVED", "COURSE_FIRED" -> {
                    val ticket = json.decodeFromString<LanTicketPayload>(envelope.payloadJson)
                    val newUiTicket = UiTicket(
                        ticketNumber = ticket.ticketNumber,
                        orderUuid = ticket.orderUuid,
                        tableLabel = ticket.tableLabel ?: "Counter",
                        station = "EXPO",
                        statusLabel = "NEW_TICKET",
                        items = ticket.itemsSummary,
                        createdAtEpochMs = ticket.createdAtEpochMs
                    )
                    _tickets.update { it + newUiTicket }
                }
                "TICKET_BUMPED" -> {
                    val bump = json.decodeFromString<LanBumpPayload>(envelope.payloadJson)
                    if (bump.newStatus == "BUMPED") {
                        _tickets.update { list -> list.filterNot { it.orderUuid == bump.orderUuid } }
                    } else {
                        _tickets.update { list ->
                            list.map {
                                if (it.orderUuid == bump.orderUuid) it.copy(statusLabel = bump.newStatus) else it
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Unparseable frame
        }
    }

    fun bumpTicket(ticket: UiTicket) {
        viewModelScope.launch {
            val nextStatus = when (ticket.statusLabel) {
                "NEW_TICKET" -> "IN_PROGRESS"
                "IN_PROGRESS" -> "READY"
                "READY" -> "BUMPED"
                else -> "BUMPED"
            }

            val bumpPayload = LanBumpPayload(
                ticketNumber = ticket.ticketNumber,
                orderUuid = ticket.orderUuid,
                newStatus = nextStatus,
                bumpedAtEpochMs = System.currentTimeMillis()
            )

            val envelope = LanEventEnvelope(
                eventId = UUID.randomUUID().toString(),
                eventType = "TICKET_BUMPED",
                timestampEpochMs = System.currentTimeMillis(),
                restaurantId = "rest_active",
                sourceTerminalId = "term_kdb_01",
                payloadJson = json.encodeToString(LanBumpPayload.serializer(), bumpPayload)
            )

            try {
                client.sendEvent(json.encodeToString(LanEventEnvelope.serializer(), envelope))
            } catch (e: Exception) {
                // Local fallback
            }

            // Optimistic update
            if (nextStatus == "BUMPED") {
                _tickets.update { list -> list.filterNot { it.orderUuid == ticket.orderUuid } }
            } else {
                _tickets.update { list ->
                    list.map {
                        if (it.orderUuid == ticket.orderUuid) it.copy(statusLabel = nextStatus) else it
                    }
                }
            }
        }
    }
}
