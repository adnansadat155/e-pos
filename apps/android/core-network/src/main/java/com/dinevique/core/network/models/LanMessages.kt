package com.dinevique.core.network.models

import kotlinx.serialization.Serializable

@Serializable
data class LanEventEnvelope(
    val eventId: String,
    val eventType: String, // 'ORDER_CREATE', 'TICKET_RECEIVED', 'TICKET_BUMPED', 'ITEM_86ED', 'HEARTBEAT'
    val timestampEpochMs: Long,
    val restaurantId: String,
    val sourceTerminalId: String,
    val payloadJson: String
)

@Serializable
data class LanTicketPayload(
    val ticketNumber: String,
    val orderUuid: String,
    val tableLabel: String?,
    val serverName: String?,
    val prepStation: String,
    val itemsSummary: List<String>,
    val createdAtEpochMs: Long
)

@Serializable
data class LanBumpPayload(
    val ticketNumber: String,
    val orderUuid: String,
    val newStatus: String, // 'IN_PROGRESS', 'BUMPED'
    val bumpedAtEpochMs: Long,
    val bumpedByEmployeeId: String? = null
)
