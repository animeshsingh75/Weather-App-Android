package com.project.weatherapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.project.weatherapp.data.model.LocationModel

class SharedPreferenceHelper {

    companion object {

        private const val WEATHER_PREF_TIME = "Weather pref time"
        private const val WEATHER_FORECAST_PREF_TIME = "Forecast pref time"
        private const val CITY_ID = "City ID"
        private var prefs: SharedPreferences? = null
        private const val LOCATION = "LOCATION"

        @Volatile
        private var instance: SharedPreferenceHelper? = null
        fun getInstance(context: Context): SharedPreferenceHelper {
            synchronized(this) {
                val _instance = instance
                if (_instance == null) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    instance = _instance
                }
                return SharedPreferenceHelper()
            }
        }
    }

    fun saveTimeOfInitialWeatherFetch(time: Long) {
        prefs?.edit(commit = true) {
            putLong(WEATHER_PREF_TIME, time)
        }
    }

    fun getTimeOfInitialWeatherFetch() = prefs?.getLong(WEATHER_PREF_TIME, 0L)

    fun saveTimeOfInitialWeatherForecastFetch(time: Long) {
        prefs?.edit(commit = true) {
            putLong(WEATHER_FORECAST_PREF_TIME, time)
        }
    }


    fun getTimeOfInitialWeatherForecastFetch() = prefs?.getLong(WEATHER_FORECAST_PREF_TIME, 0L)

    fun saveCityId(cityId: Int) {
        prefs?.edit(commit = true) {
            putInt(CITY_ID, cityId)
        }
    }

    fun getCityId() = prefs?.getInt(CITY_ID, 0)


    fun getUserSetCacheDuration() = prefs?.getString("cache_key", "0")


    fun getSelectedThemePref() = prefs?.getString("theme_key", "")

    fun saveLocation(location: LocationModel) {
        prefs?.edit(commit = true) {
            val gson = Gson()
            val json = gson.toJson(location)
            putString(LOCATION, json)
        }
    }

    fun getLocation(): LocationModel {
        val gson = Gson()
        val json = prefs?.getString(LOCATION, null)
        return gson.fromJson(json, LocationModel::class.java)
    }
}
