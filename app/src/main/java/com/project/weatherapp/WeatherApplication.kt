package com.project.weatherapp

import android.app.Application
import com.project.weatherapp.data.source.repository.WeatherRepository

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    val weatherRepository: WeatherRepository
        get() = ServiceLocator.provideWeatherRepository(this)

}