package com.dinevique.pos.payments

import com.dinevique.core.database.dao.OfflinePaymentDao
import com.dinevique.core.database.entities.OfflinePaymentQueueEntity
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Result sealed class for offline payment enqueue operations
 */
sealed class OfflinePaymentResult {
    data class Success(
        val paymentUuid: String,
        val clientOrderUuid: String,
        val amountCents: Long,
        val tipCents: Long,
        val authCode: String,
        val isOfflineAuthorized: Boolean = true
    ) : OfflinePaymentResult()

    data class Rejected(
        val reason: RejectionReason,
        val message: String
    ) : OfflinePaymentResult()
}

enum class RejectionReason {
    EXCEEDS_SINGLE_TRANSACTION_LIMIT,
    EXCEEDS_CUMULATIVE_RISK_LIMIT,
    ENCRYPTION_FAILURE,
    CARD_EXPIRED
}

/**
 * Enterprise Offline Card Authorization & Encryption Engine
 * Enforces PCI-DSS Level 1 compliant AES-256 GCM encryption and strict risk ceilings.
 */
class OfflinePaymentManager(
    private val offlinePaymentDao: OfflinePaymentDao,
    private val masterKey: SecretKey = generateDefaultKey()
) {
    companion object {
        const val MAX_SINGLE_TRANSACTION_CENTS: Long = 15_000 // $150.00
        const val MAX_CUMULATIVE_RISK_CENTS: Long = 500_000 // $5,000.00
        private const val GCM_IV_LENGTH = 12 // 96-bit IV
        private const val GCM_TAG_LENGTH = 128 // 128-bit authentication tag

        fun generateDefaultKey(): SecretKey {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            return keyGen.generateKey()
        }
    }

    /**
     * Encrypts sensitive cardholder payload using AES-256 GCM with a secure 12-byte random IV.
     * Output format: Base64( IV [12 bytes] || Ciphertext || AuthTag [16 bytes] )
     */
    fun encryptCardPayload(plaintext: String): String {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val buffer = ByteBuffer.allocate(iv.size + ciphertext.size)
        buffer.put(iv)
        buffer.put(ciphertext)

        return Base64.getEncoder().encodeToString(buffer.array())
    }

    /**
     * Decrypts AES-256 GCM ciphertext using the master key.
     */
    fun decryptCardPayload(ciphertextBase64: String): String {
        val decoded = Base64.getDecoder().decode(ciphertextBase64)
        val buffer = ByteBuffer.wrap(decoded)

        val iv = ByteArray(GCM_IV_LENGTH)
        buffer.get(iv)

        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

        val plaintextBytes = cipher.doFinal(ciphertext)
        return String(plaintextBytes, Charsets.UTF_8)
    }

    /**
     * Evaluates floor limits, encrypts card payload, and stores transaction in offline queue.
     */
    suspend fun enqueueOfflinePayment(
        clientOrderUuid: String,
        amountCents: Long,
        tipCents: Long = 0,
        cardLast4: String,
        cardBrand: String,
        rawCardToken: String
    ): OfflinePaymentResult {
        // 1. Single transaction floor limit check ($150.00)
        if (amountCents > MAX_SINGLE_TRANSACTION_CENTS) {
            return OfflinePaymentResult.Rejected(
                reason = RejectionReason.EXCEEDS_SINGLE_TRANSACTION_LIMIT,
                message = "Transaction of $${amountCents / 100.0} exceeds the $150.00 offline limit"
            )
        }

        // 2. Cumulative queue risk ceiling check ($5,000.00)
        val currentCumulative = offlinePaymentDao.getCumulativeQueuedAmountCents()
        if (currentCumulative + amountCents > MAX_CUMULATIVE_RISK_CENTS) {
            return OfflinePaymentResult.Rejected(
                reason = RejectionReason.EXCEEDS_CUMULATIVE_RISK_LIMIT,
                message = "Cumulative offline volume of $${(currentCumulative + amountCents) / 100.0} exceeds the $5,000.00 risk ceiling"
            )
        }

        // 3. Encrypt sensitive payload
        val encryptedPayload = try {
            encryptCardPayload(rawCardToken)
        } catch (e: Exception) {
            return OfflinePaymentResult.Rejected(
                reason = RejectionReason.ENCRYPTION_FAILURE,
                message = "Cryptographic initialization failure: ${e.message}"
            )
        }

        // 4. Generate local offline authorization code (e.g. OFF-74921)
        val paymentUuid = "pay_off_${UUID.randomUUID()}"
        val authCode = "OFF-${(10000..99999).random()}"

        // 5. Persist to Room SQLite
        val queueEntity = OfflinePaymentQueueEntity(
            paymentUuid = paymentUuid,
            clientOrderUuid = clientOrderUuid,
            amountCents = amountCents,
            tipCents = tipCents,
            encryptedCardPayload = encryptedPayload,
            cardLast4 = cardLast4,
            cardBrand = cardBrand,
            capturedAtEpochMs = System.currentTimeMillis(),
            settlementStatus = "QUEUED"
        )

        offlinePaymentDao.enqueuePayment(queueEntity)

        return OfflinePaymentResult.Success(
            paymentUuid = paymentUuid,
            clientOrderUuid = clientOrderUuid,
            amountCents = amountCents,
            tipCents = tipCents,
            authCode = authCode,
            isOfflineAuthorized = true
        )
    }
}
