package com.example.sunshine.ui.weather

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sunshine.R
import com.example.sunshine.databinding.ActivityWeatherDetailsBinding
import com.example.sunshine.db.entity.WeatherParams
import com.example.sunshine.utils.Constants.API_KEY
import com.example.sunshine.utils.FunctionUtils
import com.example.sunshine.utils.FunctionUtils.focusScreen
import com.example.sunshine.utils.FunctionUtils.snackBar
import com.example.sunshine.utils.FunctionUtils.toast
import com.example.sunshine.utils.RequestStatus
import com.example.sunshine.viewmodel.weather.WeatherDetailActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class WeatherDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherDetailsBinding
    private lateinit var viewModel: WeatherDetailActivityViewModel
    private lateinit var weatherDialog: Dialog
    private lateinit var currentCity: String   /*  keeps track of the last properly entered / retrieved city name to keep the
                                                   locationTxt updated in case of wrong city name being entered  */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherDetailsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[WeatherDetailActivityViewModel::class.java]
        setContentView(binding.root)

        setUpUI()
        handleLiveData()
        setUpClickListeners()

        if (isLocationPermissionGranted())  //  permission already granted
            getDeviceLocation()
    }

    private fun setUpClickListeners() {
        binding.locationImg.setOnClickListener { loadBottomSheet() }
    }

    private fun loadBottomSheet() {
        val dialog = BottomSheetDialog(binding.root.context)
        dialog.setContentView(R.layout.select_city_bottom_sheet)

        val cityName = dialog.findViewById<TextInputEditText>(R.id.cityEdit)
        val cityContainer = dialog.findViewById<TextInputLayout>(R.id.cityContainer)
        val cityBtn = dialog.findViewById<ConstraintLayout>(R.id.cityBtn)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)

        cityBtn?.setOnClickListener {
            progressBar?.visibility = View.VISIBLE
            if (cityName?.text?.isBlank() == true) {  //  No city name entered
                cityContainer?.isErrorEnabled = true
                cityContainer?.error = getString(R.string.please_enter_the_city_name)
                progressBar?.visibility = View.INVISIBLE
                cityContainer?.rootView?.let { it1 -> FunctionUtils.animateView(it1) }
                FunctionUtils.vibrateDevice(binding.root.context)
            } else {  //  City name entered, attempt to update weather for that city
                weatherDialog.show()
                cityContainer?.isErrorEnabled = false
                binding.locationTxt.text = cityName?.text.toString().trim().replaceFirstChar { line -> if (line.isLowerCase()) line.titlecase(Locale.ROOT) else line.toString() }
                progressBar?.visibility = View.INVISIBLE
                dialog.dismiss()
                viewModel.loadWeatherData(binding.locationTxt.text.toString(), API_KEY, binding.root.context)
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleLiveData() {
        handleGetDeviceLocationLiveData()
        handleGetCityNameFromLocationLiveData()
        handleLoadWeatherDataLiveData()
        handleGetWeatherFromDbLiveData()
    }

    private fun setUpUI() {
        hideStatusBar()
        focusScreen(binding.root)
        weatherDialog = FunctionUtils.setUpDialog(getString(R.string.getting_weather_details), binding.root.context)
    }

    @Suppress("DEPRECATION")
    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        viewModel.getDeviceLocation(this)
    }

    private fun handleGetDeviceLocationLiveData() {
        viewModel.getDeviceLocationLiveData.observe(this) {
            when (it.status) {
                RequestStatus.LOADING -> {
                    weatherDialog.show()
                }
                RequestStatus.SUCCESS -> {
                    if (it.data != null)   //  location retrieved, get city name from location coordinates
                        viewModel.getCityNameFromLocation(it.data, API_KEY, binding.root.context)
                }
                RequestStatus.EXCEPTION -> {
                    toast(binding.root.context, it.message.toString())
                    weatherDialog.dismiss()
                }
            }
        }
    }


    private fun handleGetCityNameFromLocationLiveData() {
        viewModel.getCityNameFromLocationLiveData.observe(this) {
            with(binding) {
                when (it.status) {
                    RequestStatus.LOADING -> {
                    }
                    RequestStatus.SUCCESS -> {
                        if (it != null) {      //  city name retrieved, load the city's weather reports
                            currentCity = it.data!!.name
                            locationTxt.text = currentCity
                            viewModel.loadWeatherData(currentCity, API_KEY, binding.root.context)
                        } else {
                            toast(binding.root.context, getString(R.string.some_error_occurred))
                        }
                    }
                    RequestStatus.EXCEPTION -> {   //   no internet available, load last saved weather reports from database
                        viewModel.getLastWeatherFromDb()
                        weatherDialog.dismiss()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleLoadWeatherDataLiveData() {
        viewModel.loadWeatherDataLiveData.observe(this) {
            when (it.status) {
                RequestStatus.LOADING -> {
                }
                RequestStatus.SUCCESS -> {
                    if (it != null) {     //  weather reports retrieved, attempt to save it to room database
                        currentCity = it.data!!.name
                        val currentTime = Date().time / 1000
                        val weatherParams = WeatherParams(
                            currentCity, it.data.main.temp.toString(), it.data.weather[0].description, it.data.visibility.toString(),
                            it.data.wind.speed.toString(), it.data.main.humidity.toString(), it.data.main.pressure.toString(),
                            it.data.sys.sunrise.toString(), it.data.sys.sunset.toString(), currentTime.toString()
                        )
                        viewModel.clearDb()
                        viewModel.addWeather(weatherParams)
                        updateUI(weatherParams)
                    } else {
                        toast(binding.root.context, getString(R.string.some_error_occurred))
                    }
                }
                RequestStatus.EXCEPTION -> {
                    if (it.message.toString() == getString(R.string.response_not_successful)) {
                        toast(binding.root.context, getString(R.string.invalid_city_name))
                    } else {
                        toast(binding.root.context, it.message.toString())
                    }
                    binding.locationTxt.text = currentCity
                    weatherDialog.dismiss()
                }
            }
        }
    }

    private fun updateUI(weatherParams: WeatherParams) {   //  update the UI according to day or night time

        if (weatherParams.time.toLong() in weatherParams.sunrise.toLong() until weatherParams.sunset.toLong()) {  // day-time
            setDay()
        } else {      // night-time
            setNight()
        }

        with(binding) {
            locationTxt.text = weatherParams.cityName
            locationImg.visibility = View.VISIBLE
            degreeCelsiusTxt.visibility = View.VISIBLE
            bottomCardView.visibility = View.VISIBLE
            currentTemp.text = weatherParams.currentTemp.toDouble().minus(273.15).roundToInt().toString()
            currentWeather.text = weatherParams.currentWeather.replaceFirstChar { line -> if (line.isLowerCase()) line.titlecase(Locale.ROOT) else line.toString() }
            val visibility = weatherParams.visibility.toLong().floorDiv(1000).toString()
            currentVisibility.text = String.format(getString(R.string.visibilityInKm), visibility)
            currentHumidity.text = String.format(getString(R.string.humidityInPercentage), weatherParams.humidity)
            val windSpeed = weatherParams.windSpeed.toDouble().times(3.6).roundToInt()
            currentWindSpeed.text = String.format(getString(R.string.windSpeedInMS), windSpeed)
            currentPressure.text = String.format(getString(R.string.pressureInhPa), weatherParams.pressure)
            weatherDialog.dismiss()
        }
    }

    private fun setDay() {
        with(binding) {
            bg.background = ContextCompat.getDrawable(binding.root.context, R.drawable.day_bg)
            locationTxt.setTextColor(getColor(R.color.black_white))
            locationImg.setColorFilter(getColor(R.color.gray_day))
            degreeCelsiusTxt.setTextColor(getColor(R.color.gray_day))
            currentTemp.setTextColor(getColor(R.color.black_white))
            currentWeather.setTextColor(getColor(R.color.black_white))
            currentHumidity.setTextColor(getColor(R.color.black_white))
            currentWindSpeed.setTextColor(getColor(R.color.black_white))
            currentVisibility.setTextColor(getColor(R.color.black_white))
            currentPressure.setTextColor(getColor(R.color.black_white))
            humidityTxt.setTextColor(getColor(R.color.gray_day))
            windSpeedTxt.setTextColor(getColor(R.color.gray_day))
            visibilityTxt.setTextColor(getColor(R.color.gray_day))
            pressureTxt.setTextColor(getColor(R.color.gray_day))
        }
    }

    private fun setNight() {
        with(binding) {
            bg.background = ContextCompat.getDrawable(binding.root.context, R.drawable.night_bg)
            locationTxt.setTextColor(getColor(R.color.white_black))
            locationImg.setColorFilter(getColor(R.color.gray_night))
            degreeCelsiusTxt.setTextColor(getColor(R.color.gray_night))
            currentTemp.setTextColor(getColor(R.color.white_black))
            currentWeather.setTextColor(getColor(R.color.white_black))
            currentHumidity.setTextColor(getColor(R.color.white_black))
            currentWindSpeed.setTextColor(getColor(R.color.white_black))
            currentVisibility.setTextColor(getColor(R.color.white_black))
            currentPressure.setTextColor(getColor(R.color.white_black))
            humidityTxt.setTextColor(getColor(R.color.gray_night))
            windSpeedTxt.setTextColor(getColor(R.color.gray_night))
            visibilityTxt.setTextColor(getColor(R.color.gray_night))
            pressureTxt.setTextColor(getColor(R.color.gray_night))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGetWeatherFromDbLiveData() {
        viewModel.getWeatherFromDbLiveData.observe(this) {
            if (it != null) {   //  retrieved saved weather reports from database, display it
                val dateTime = Instant.ofEpochSecond(it.time.toLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().toString().replace("T0", " ")   //  garbage value
                setUpSnackBar(String.format(getString(R.string.no_internet_showing_reports_from), dateTime)).show()
                currentCity = it.cityName
                updateUI(it)
            } else {
                toast(binding.root.context, getString(R.string.no_internet_unable_to_retrieve))
            }
        }
    }

    private fun setUpSnackBar(msg: String): Snackbar {
        val snackBar = snackBar(binding.root.rootView, msg, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(getString(R.string.dismiss)) { snackBar.dismiss() }
        val view = snackBar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER
        view.layoutParams = params
        return snackBar
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                getDeviceLocation()
            else {
                snackBar(binding.root, getString(R.string.permission_denied)).show()
            }
        }
    }
}