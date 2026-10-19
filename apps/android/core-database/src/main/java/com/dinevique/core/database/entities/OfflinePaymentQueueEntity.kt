package com.dinevique.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Encrypted Offline Card Authorization Queue
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.2
 */
@Entity(tableName = "offline_payment_queue")
data class OfflinePaymentQueueEntity(
    @PrimaryKey
    @ColumnInfo(name = "payment_uuid")
    val paymentUuid: String,

    @ColumnInfo(name = "client_order_uuid")
    val clientOrderUuid: String,

    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,

    @ColumnInfo(name = "tip_cents")
    val tipCents: Long = 0,

    @ColumnInfo(name = "encrypted_card_payload")
    val encryptedCardPayload: String, // AES-256 GCM encrypted ciphertext

    @ColumnInfo(name = "card_last4")
    val cardLast4: String,

    @ColumnInfo(name = "card_brand")
    val cardBrand: String,

    @ColumnInfo(name = "captured_at_epoch_ms")
    val capturedAtEpochMs: Long,

    @ColumnInfo(name = "settlement_status")
    val settlementStatus: String = "QUEUED" // 'QUEUED', 'SETTLED', 'FAILED'
)
