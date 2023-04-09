package com.example.sunshine.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.example.sunshine.db.SunshineDatabase
import com.example.sunshine.db.entity.WeatherParams
import com.example.sunshine.model.WeatherModel
import com.example.sunshine.service.RetrofitInstance
import com.example.sunshine.utils.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val database: SunshineDatabase) {

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context, getDeviceLocationLiveData: MutableLiveData<Resource<Location?>>) {

        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    getDeviceLocationLiveData.value = Resource.error(null, "Cannot get location")
                else {
                    getDeviceLocationLiveData.value = Resource.success(location)
                }
            }
    }

    suspend fun getCityNameFromLocation(location: Location?, API_KEY: String): Resource<WeatherModel> {
        val response = try {
            RetrofitInstance.api.getCurrentCity(location!!.latitude.toString(), location.longitude.toString(), API_KEY, "en")
        } catch (e: IOException) {
            return Resource.error(null, "IOException, you might not have internet connection")
        } catch (e: HttpException) {
            return Resource.error(null, "HttpException, unexpected response")
        }
        return if (response.isSuccessful && response.body() != null)
            Resource.success(response.body())
        else {
            Resource.error(null, "Response not successful")
        }
    }

    suspend fun loadWeatherData(cityName: String, API_KEY: String): Resource<WeatherModel> {

        val response = try {
            RetrofitInstance.api.getPosts(cityName, API_KEY)
        } catch (e: IOException) {
            return Resource.error(null, "IOException, you might not have internet connection")
        } catch (e: HttpException) {
            return Resource.error(null, "HttpException, unexpected response")
        }
        return if (response.isSuccessful && response.body() != null) {
            Resource.success(response.body())
        } else {
            Resource.error(null, "Response not successful")
        }
    }

    suspend fun addWeather(weatherParams: WeatherParams) {
        database.weatherDao!!.insertWeather(weatherParams)
    }

    suspend fun getLastWeatherFromDb(): WeatherParams? {
        return database.weatherDao!!.getWeather()?.get(0)
    }

    suspend fun clearDb() {
        database.weatherDao!!.clearDb()
    }
}
