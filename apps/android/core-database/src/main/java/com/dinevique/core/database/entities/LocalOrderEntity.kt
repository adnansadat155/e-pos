package com.dinevique.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local Order State Entity (Android Room SQLite)
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.2
 */
@Entity(tableName = "local_orders")
data class LocalOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_order_uuid")
    val clientOrderUuid: String,

    @ColumnInfo(name = "restaurant_id")
    val restaurantId: String,

    @ColumnInfo(name = "table_label")
    val tableLabel: String?,

    @ColumnInfo(name = "source_channel")
    val sourceChannel: String, // 'POS', 'HANDHELD', 'ONLINE'

    @ColumnInfo(name = "ticket_status")
    val ticketStatus: String, // 'NEW_TICKET', 'IN_PROGRESS', 'READY', 'PAID', 'BUMPED'

    @ColumnInfo(name = "order_payload_json")
    val orderPayloadJson: String,

    @ColumnInfo(name = "subtotal_cents")
    val subtotalCents: Long,

    @ColumnInfo(name = "tax_cents")
    val taxCents: Long,

    @ColumnInfo(name = "tip_cents")
    val tipCents: Long = 0,

    @ColumnInfo(name = "total_cents")
    val totalCents: Long,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,

    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)
