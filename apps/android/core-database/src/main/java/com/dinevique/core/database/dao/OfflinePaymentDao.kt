package com.dinevique.core.database.dao

import androidx.room.*
import com.dinevique.core.database.entities.OfflinePaymentQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePaymentDao {
    @Query("SELECT * FROM offline_payment_queue WHERE settlement_status = 'QUEUED' ORDER BY captured_at_epoch_ms ASC")
    fun observeQueuedPayments(): Flow<List<OfflinePaymentQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueuePayment(payment: OfflinePaymentQueueEntity)

    @Query("SELECT COALESCE(SUM(amount_cents), 0) FROM offline_payment_queue WHERE settlement_status = 'QUEUED'")
    suspend fun getCumulativeQueuedAmountCents(): Long

    @Query("UPDATE offline_payment_queue SET settlement_status = :status WHERE payment_uuid = :paymentUuid")
    suspend fun updateSettlementStatus(paymentUuid: String, status: String)

    @Query("SELECT COUNT(*) FROM offline_payment_queue WHERE settlement_status = 'QUEUED'")
    suspend fun getQueuedCount(): Int
}
