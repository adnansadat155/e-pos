package com.dinevique.core.database.dao

import androidx.room.*
import com.dinevique.core.database.entities.LocalOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM local_orders ORDER BY created_at_epoch_ms DESC")
    fun observeAllOrders(): Flow<List<LocalOrderEntity>>

    @Query("SELECT * FROM local_orders WHERE ticket_status IN ('NEW_TICKET', 'IN_PROGRESS', 'READY') ORDER BY created_at_epoch_ms ASC")
    fun observeActiveKitchenOrders(): Flow<List<LocalOrderEntity>>

    @Query("SELECT * FROM local_orders WHERE table_label = :tableLabel AND ticket_status != 'PAID' LIMIT 1")
    suspend fun getActiveOrderByTable(tableLabel: String): LocalOrderEntity?

    @Query("SELECT * FROM local_orders WHERE client_order_uuid = :orderUuid LIMIT 1")
    suspend fun getOrderByUuid(orderUuid: String): LocalOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrder(order: LocalOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<LocalOrderEntity>)

    @Query("UPDATE local_orders SET ticket_status = :status, updated_at_epoch_ms = :nowMs WHERE client_order_uuid = :orderUuid")
    suspend fun updateOrderStatus(orderUuid: String, status: String, nowMs: Long)

    @Query("DELETE FROM local_orders WHERE client_order_uuid = :orderUuid")
    suspend fun deleteOrder(orderUuid: String)
}
