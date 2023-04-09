package com.example.sunshine.viewmodel.weather

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sunshine.db.entity.WeatherParams
import com.example.sunshine.model.WeatherModel
import com.example.sunshine.repository.WeatherRepository
import com.example.sunshine.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherDetailActivityViewModel @Inject constructor(private val repository: WeatherRepository) : ViewModel() {

    var getDeviceLocationLiveData = MutableLiveData<Resource<Location?>>()
    fun getDeviceLocation(context: Context) {
        getDeviceLocationLiveData.postValue(Resource.loading(null))
        viewModelScope.launch {
            repository.getDeviceLocation(context, getDeviceLocationLiveData)
        }
    }

    var getCityNameFromLocationLiveData = MutableLiveData<Resource<WeatherModel>>()
    fun getCityNameFromLocation(location: Location?, API_KEY: String, context: Context) {
        getCityNameFromLocationLiveData.postValue(Resource.loading(null))
        viewModelScope.launch {
            getCityNameFromLocationLiveData.postValue(repository.getCityNameFromLocation(location, API_KEY, context))
        }
    }

    var loadWeatherDataLiveData = MutableLiveData<Resource<WeatherModel>>()
    fun loadWeatherData(cityName: String, API_KEY: String, context: Context) {
        loadWeatherDataLiveData.postValue(Resource.loading(null))
        viewModelScope.launch {
            loadWeatherDataLiveData.value = repository.loadWeatherData(cityName, API_KEY, context)
        }
    }

    fun addWeather(weatherParams: WeatherParams) {
        viewModelScope.launch {
            repository.addWeather(weatherParams)
        }
    }

    var getWeatherFromDbLiveData = MutableLiveData<WeatherParams>()
    fun getLastWeatherFromDb() {
        viewModelScope.launch {
            getWeatherFromDbLiveData.postValue(repository.getLastWeatherFromDb())
        }
    }


    fun clearDb() {
        viewModelScope.launch {
            repository.clearDb()
        }
    }
}