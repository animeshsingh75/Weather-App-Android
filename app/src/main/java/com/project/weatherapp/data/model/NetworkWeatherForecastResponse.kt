package com.project.weatherapp.data.model

import com.google.gson.annotations.SerializedName
import com.project.weatherapp.data.model.City
import com.project.weatherapp.data.model.NetworkWeatherForecast

data class NetworkWeatherForecastResponse(

    @SerializedName("list")
    val weathers: List<NetworkWeatherForecast>,

    val city: City
)
