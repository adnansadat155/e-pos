package com.dinevique.pos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dinevique.core.network.KtorLanServer

/**
 * Android Foreground Service hosting the embedded Ktor LAN server.
 * Ensures the Master Terminal continues serving local POS clients even in the background.
 */
class MasterTerminalService : Service() {
    private val ktorServer = KtorLanServer(port = 8080)

    companion object {
        const val CHANNEL_ID = "dinevique_master_orchestrator"
        const val NOTIFICATION_ID = 4401
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        ktorServer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        ktorServer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Dinevique Master Orchestrator",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains in-store LAN ordering server and peripheral control"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dinevique Master Terminal Active")
            .setContentText("Serving in-store LAN traffic on port 8080")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
    }
}
