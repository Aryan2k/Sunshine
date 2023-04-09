package com.example.sunshine.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sunshine.db.dao.WeatherDao
import com.example.sunshine.db.entity.WeatherParams

@Database(entities = [WeatherParams::class], exportSchema = false, version = 1)
abstract class SunshineDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao?
}