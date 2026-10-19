package com.dinevique.pos.payments

import com.dinevique.core.database.dao.OfflinePaymentDao
import com.dinevique.core.database.entities.OfflinePaymentQueueEntity
import kotlinx.coroutines.flow.first

data class BatchDrainResult(
    val totalProcessed: Int,
    val settledCount: Int,
    val failedCount: Int,
    val totalSettledCents: Long,
    val isWanOnline: Boolean
)

/**
 * WAN Connectivity Observer and Store-and-Forward Settlement Replay Engine.
 * Automatically drains offline encrypted payment authorizations upon network recovery.
 */
class PaymentDrainManager(
    private val offlinePaymentDao: OfflinePaymentDao,
    private val httpClient: (url: String, payload: String) -> Pair<Int, String> = { _, _ -> Pair(200, "{\"success\":true}") }
) {
    private var isWanConnected: Boolean = true

    fun setWanConnectivity(connected: Boolean) {
        this.isWanConnected = connected
    }

    fun isOnline(): Boolean = isWanConnected

    /**
     * Drains all queued offline authorizations chronologically in batches.
     */
    suspend fun drainQueuedPayments(
        gatewayBaseUrl: String,
        restaurantId: String,
        terminalId: String
    ): BatchDrainResult {
        if (!isWanConnected) {
            return BatchDrainResult(
                totalProcessed = 0,
                settledCount = 0,
                failedCount = 0,
                totalSettledCents = 0,
                isWanOnline = false
            )
        }

        val queuedPayments = offlinePaymentDao.observeQueuedPayments().first()
        if (queuedPayments.isEmpty()) {
            return BatchDrainResult(
                totalProcessed = 0,
                settledCount = 0,
                failedCount = 0,
                totalSettledCents = 0,
                isWanOnline = true
            )
        }

        var settledCount = 0
        var failedCount = 0
        var totalSettledCents = 0L

        // Formulate batch payload
        val paymentEntriesJson = queuedPayments.joinToString(separator = ",", prefix = "[", postfix = "]") { p ->
            """{
                "paymentUuid": "${p.paymentUuid}",
                "clientOrderUuid": "${p.clientOrderUuid}",
                "amountCents": ${p.amountCents},
                "tipCents": ${p.tipCents},
                "encryptedCardPayload": "${p.encryptedCardPayload}",
                "cardLast4": "${p.cardLast4}",
                "cardBrand": "${p.cardBrand}",
                "capturedAtEpochMs": ${p.capturedAtEpochMs}
            }""".trimIndent()
        }

        val batchPayload = """{
            "restaurantId": "$restaurantId",
            "terminalId": "$terminalId",
            "payments": $paymentEntriesJson
        }""".trimIndent()

        val settleUrl = "$gatewayBaseUrl/api/v1/payments/batch-settle"
        val (statusCode, responseBody) = try {
            httpClient(settleUrl, batchPayload)
        } catch (e: Exception) {
            Pair(500, e.message ?: "Network error")
        }

        if (statusCode in 200..299) {
            for (payment in queuedPayments) {
                offlinePaymentDao.updateSettlementStatus(payment.paymentUuid, "SETTLED")
                settledCount++
                totalSettledCents += (payment.amountCents + payment.tipCents)
            }
        } else {
            // Check for stale authorizations exceeding 72h TTL
            val now = System.currentTimeMillis()
            val maxTtlMs = 72L * 60 * 60 * 1000 // 72 hours
            for (payment in queuedPayments) {
                if (now - payment.capturedAtEpochMs > maxTtlMs) {
                    offlinePaymentDao.updateSettlementStatus(payment.paymentUuid, "EXPIRED_MANUAL_REVIEW")
                    failedCount++
                }
            }
        }

        return BatchDrainResult(
            totalProcessed = queuedPayments.size,
            settledCount = settledCount,
            failedCount = failedCount,
            totalSettledCents = totalSettledCents,
            isWanOnline = true
        )
    }
}
