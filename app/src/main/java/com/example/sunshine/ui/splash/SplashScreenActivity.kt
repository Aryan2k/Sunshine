package com.example.sunshine.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.example.sunshine.databinding.ActivitySplashScreenBinding
import com.example.sunshine.ui.weather.WeatherDetailsActivity
import com.example.sunshine.utils.FunctionUtils.animateView

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
        animateView(binding.appNameTxt, duration = 1000, techniques = Techniques.FadeIn)
        delayScreen()
    }

    private fun delayScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WeatherDetailsActivity::class.java))
            finish()
        }, 1500)
    }

    @Suppress("DEPRECATION")
    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()
    }
}