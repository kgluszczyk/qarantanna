package com.gluszczykk.qarantanna

import android.app.Application
import androidx.room.Room

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "config"
        ).build()
    }
}