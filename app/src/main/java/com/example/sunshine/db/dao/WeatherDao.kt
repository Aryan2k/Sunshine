package com.example.sunshine.db.dao

import androidx.room.*
import com.example.sunshine.db.entity.WeatherParams

@Dao
interface WeatherDao {

    //returns the id of inserted item
    @Insert
    suspend fun insertWeather(weatherParams: WeatherParams): Long

    @Query("SELECT * FROM WeatherParams")
    suspend fun getWeather(): List<WeatherParams>?

    @Query("DELETE FROM WeatherParams")
    suspend fun clearDb()
}