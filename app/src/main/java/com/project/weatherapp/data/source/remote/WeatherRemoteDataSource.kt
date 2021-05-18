package com.project.weatherapp.data.source.remote

import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.NetworkWeather
import com.project.weatherapp.data.model.NetworkWeatherForecast
import com.project.weatherapp.utils.Result

interface WeatherRemoteDataSource {
    suspend fun getWeather(location: LocationModel): Result<NetworkWeather>

    suspend fun getWeatherForecast(location: LocationModel): Result<List<NetworkWeatherForecast>>

    suspend fun getSearchWeather(query: String): Result<NetworkWeather>
}