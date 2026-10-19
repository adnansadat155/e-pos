package com.dinevique.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dinevique.core.database.converters.Converters
import com.dinevique.core.database.dao.OfflinePaymentDao
import com.dinevique.core.database.dao.OrderDao
import com.dinevique.core.database.dao.OutboundQueueDao
import com.dinevique.core.database.entities.LocalOrderEntity
import com.dinevique.core.database.entities.OfflinePaymentQueueEntity
import com.dinevique.core.database.entities.OutboundEventQueueEntity

@Database(
    entities = [
        LocalOrderEntity::class,
        OutboundEventQueueEntity::class,
        OfflinePaymentQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DineviqueDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun outboundQueueDao(): OutboundQueueDao
    abstract fun offlinePaymentDao(): OfflinePaymentDao
}
