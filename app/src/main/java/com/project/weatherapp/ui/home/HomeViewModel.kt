package com.project.weatherapp.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.Weather
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.ui.forecast.CITY_ID
import com.project.weatherapp.ui.settings.UNIT_SELECTED
import com.project.weatherapp.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val LAST_UPDATED_TIME = "lastUpdatedTime"

class HomeViewModel(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)
    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading = _isLoading.asLiveData()
    private val _dataFetchState = MutableLiveData<Boolean>()
    val dataFetchState = _dataFetchState.asLiveData()
    private val _firstTimeNoInternet = MutableLiveData<Boolean>()
    val firstTimeNoInternet = _firstTimeNoInternet.asLiveData()
    private val _weather = MutableLiveData<Weather?>()
    val weather = _weather.asLiveData()
    private var _lastUpdatedTime = MutableLiveData<String>()
    val lastUpdatedTime = _lastUpdatedTime.asLiveData()
    val sPref = PreferenceManager.getDefaultSharedPreferences(getApplication())
    val getUnitType=sPref.getString(UNIT_SELECTED,"Metric")
    fun getLocationLiveData() = locationLiveData
    fun getWeather(location: LocationModel) {
        _isLoading.postValue(true)
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = repository.getWeather(location, false)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        val weather = result.data
                        sPref.edit().putInt(CITY_ID,weather.cityId).apply()
                        Log.d("Weather",sPref.getInt(CITY_ID,0).toString())
                        _dataFetchState.value = true
                        _weather.value = weather
                        if (isNetworkConnected(getApplication<WeatherApplication>())) {
                            _lastUpdatedTime.value = currentSystemTime()
                            sPref.edit().putString(LAST_UPDATED_TIME, _lastUpdatedTime.value)
                                .apply()
                        } else {
                            _lastUpdatedTime.value = sPref.getString(LAST_UPDATED_TIME, null)
                        }
                    } else {
                        refreshWeather(location)
                    }
                }
                is Error -> {
                    _isLoading.value = false
                    _dataFetchState.value = false
                }

                is Result.Loading -> _isLoading.postValue(true)
            }
        }
    }

    fun refreshWeather(location: LocationModel) {
        _isLoading.value = true
        GlobalScope.launch(Dispatchers.Main) {
            if (!isNetworkConnected(getApplication<WeatherApplication>())) {
                _firstTimeNoInternet.value = true
            }
            when (val result = repository.getWeather(location, true)) {
                is Result.Success -> {
                    _isLoading.value = false
                    _firstTimeNoInternet.value = false
                    if (result.data != null) {
                        val weather = result.data.apply {
                            if(getUnitType=="Metric"){
                                this.networkWeatherCondition.temp =
                                    convertKelvinToCelsius(this.networkWeatherCondition.temp)
                            }else{
                                this.networkWeatherCondition.temp =
                                    convertKelvinToCelsius(this.networkWeatherCondition.temp)
                            }

                        }
                        _lastUpdatedTime.value = currentSystemTime()
                        sPref.edit().putString(LAST_UPDATED_TIME, _lastUpdatedTime.value).apply()
                        _dataFetchState.value = true
                        _weather.value = weather
                        sPref.edit().putInt(CITY_ID,weather.cityId).apply()
                        Log.d("Weather",sPref.getInt(CITY_ID,0).toString())
                        repository.deleteWeatherData()
                        repository.storeWeatherData(weather)
                    } else {
                        _weather.postValue(null)
                        _dataFetchState.postValue(false)
                    }
                }
                is Error -> {
                    _firstTimeNoInternet.value = true
                    _isLoading.value = false
                    _dataFetchState.value = false
                }
                is Result.Loading -> {
                    _isLoading.postValue(true)
                }
            }
        }
    }
    fun doneRefreshing() {
        _isLoading.value = null
    }

    @Suppress("UNCHECKED_CAST")
    class HomeFragmentViewModelFactory(
        private val repository: WeatherRepository,
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            (HomeViewModel(repository, application) as T)
    }

}