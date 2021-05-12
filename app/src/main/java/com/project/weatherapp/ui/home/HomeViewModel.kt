package com.project.weatherapp.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.Weather
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

const val LAST_UPDATED_TIME="lastUpdatedTime"
class HomeViewModel(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)
    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading = _isLoading.asLiveData()
    private val _dataFetchState = MutableLiveData<Boolean>()
    val dataFetchState = _dataFetchState.asLiveData()
    private val _firstTimeNoInternet= MutableLiveData<Boolean>()
    val firstTimeNoInternet=_firstTimeNoInternet.asLiveData()
    private val _weather = MutableLiveData<Weather?>()
    val weather = _weather.asLiveData()
    private var _lastUpdatedTime = MutableLiveData<String>()
    val lastUpdatedTime = _lastUpdatedTime.asLiveData()
    fun getLocationLiveData() = locationLiveData
    fun getWeather(location: LocationModel) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            when (val result = repository.getWeather(location, false)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        val weather = result.data
                        _dataFetchState.value = true
                        _weather.value = weather
                        val sPref= PreferenceManager.getDefaultSharedPreferences(getApplication())
                        if (isNetworkConnected(getApplication<WeatherApplication>())) {
                            _lastUpdatedTime.value=currentSystemTime()
                            sPref.edit().putString(LAST_UPDATED_TIME,_lastUpdatedTime.value).apply()
                            Log.d("Location",location.toString())
                        } else {
                            _lastUpdatedTime.value=sPref.getString(LAST_UPDATED_TIME,null)
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
        Log.d("Location50","Here1")
        _isLoading.value = true
        GlobalScope.launch(Dispatchers.Main){
            Log.d("Location50","Here2")
            if(!isNetworkConnected(getApplication<WeatherApplication>())){
                _firstTimeNoInternet.value=true
                
            }
            when (val result = repository.getWeather(location, true)) {
                is Result.Success -> {
                    Log.d("Location50",location.toString())
                    _isLoading.value = false
                    _firstTimeNoInternet.value=false
                    if(isNetworkConnected(getApplication<WeatherApplication>())){
                        Log.d("Location50","Here3")
                        _firstTimeNoInternet.value=false
                        if (result.data != null) {
                            Log.d("Location",result.data.toString())
                            val weather = result.data.apply {
                                this.networkWeatherCondition.temp =
                                    convertKelvinToCelsius(this.networkWeatherCondition.temp)
                            }
                            val sPref= PreferenceManager.getDefaultSharedPreferences(getApplication())
                            _lastUpdatedTime.value = currentSystemTime()
                            sPref.edit().putString(LAST_UPDATED_TIME,_lastUpdatedTime.value).apply()
                            _dataFetchState.value = true
                            _weather.value = weather
                            repository.deleteWeatherData()
                            repository.storeWeatherData(weather)
                        } else {
                            Log.d("Location50","Here4")
                            _weather.postValue(null)
                            _dataFetchState.postValue(false)
                        }
                    }else{
                        _firstTimeNoInternet.value=true
                    }

                }
                is Error -> {
                    Log.d("Location50","Here5")
                    _firstTimeNoInternet.value=true
                    _isLoading.value = false
                    _dataFetchState.value = false
                }
                is Result.Loading -> {
                    Log.d("Location50","Here6")
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