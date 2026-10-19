package com.dinevique.core.database.dao

import androidx.room.*
import com.dinevique.core.database.entities.OutboundEventQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboundQueueDao {
    @Query("SELECT * FROM outbound_event_queue WHERE sync_status = 'PENDING' ORDER BY created_at_epoch_ms ASC")
    fun observePendingEvents(): Flow<List<OutboundEventQueueEntity>>

    @Query("SELECT * FROM outbound_event_queue WHERE sync_status = 'PENDING' ORDER BY created_at_epoch_ms ASC LIMIT :batchSize")
    suspend fun getPendingBatch(batchSize: Int = 50): List<OutboundEventQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueEvent(event: OutboundEventQueueEntity)

    @Query("UPDATE outbound_event_queue SET sync_status = :status, retry_count = retry_count + 1 WHERE event_uuid = :eventUuid")
    suspend fun updateEventStatus(eventUuid: String, status: String)

    @Query("DELETE FROM outbound_event_queue WHERE event_uuid = :eventUuid")
    suspend fun removeAckedEvent(eventUuid: String)

    @Query("DELETE FROM outbound_event_queue WHERE event_uuid IN (:eventUuids)")
    suspend fun removeAckedEvents(eventUuids: List<String>)
}
