package com.dinevique.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Outbound Cloud Sync Queue (FIFO Store-and-Forward)
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.2
 */
@Entity(tableName = "outbound_event_queue")
data class OutboundEventQueueEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_uuid")
    val eventUuid: String,

    @ColumnInfo(name = "event_type")
    val eventType: String, // 'ORDER_CREATE', 'TICKET_BUMPED', 'ORDER_PAID', 'ITEM_86ED'

    @ColumnInfo(name = "entity_uuid")
    val entityUuid: String,

    @ColumnInfo(name = "payload_json")
    val payloadJson: String,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING", // 'PENDING', 'IN_FLIGHT', 'ACKED', 'FAILED'

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long
)
