package com.dinevique.pos

import android.app.Application
import android.content.Intent
import androidx.room.Room
import com.dinevique.core.database.DineviqueDatabase

class PosApplication : Application() {
    lateinit var database: DineviqueDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            DineviqueDatabase::class.java,
            "dinevique_master_terminal.db"
        ).fallbackToDestructiveMigration().build()

        // Start Ktor Master Terminal Orchestrator Service
        val serviceIntent = Intent(this, MasterTerminalService::class.java)
        startForegroundService(serviceIntent)
    }
}
