package com.example.aonime

import android.app.Application
import androidx.room.Room
import com.example.aonime.data.AonimeDatabase
import com.example.aonime.data.AonimeRepository

class AonimeApp : Application() {

    lateinit var database: AonimeDatabase
        private set

    lateinit var repository: AonimeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AonimeDatabase::class.java,
            "aonime_db",
        ).build()
        repository = AonimeRepository(favoriteDao = database.favoriteDao())
    }
}
