package com.example.sunshine.service

import com.example.sunshine.model.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getPosts(@Query("q") cityName: String, @Query("appid") api_key: String): Response<WeatherModel>

    @GET("weather")
    suspend fun getCurrentCity(@Query("lat") latitude: String, @Query("lon") longitude: String, @Query("appid") api_key: String, @Query("lang") language: String): Response<WeatherModel>
}