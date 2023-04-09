package com.example.sunshine.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherParams(
    val cityName: String,
    val currentTemp: String,
    val currentWeather: String,
    val visibility: String,
    val windSpeed: String,
    val humidity: String,
    val pressure: String,
    val sunrise: String,
    val sunset: String,
    val time: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long =0L
)
