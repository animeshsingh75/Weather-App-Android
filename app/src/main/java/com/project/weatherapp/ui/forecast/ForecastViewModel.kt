package com.project.weatherapp.ui.forecast

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.WeatherForecast
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.ui.home.LAST_UPDATED_TIME
import com.project.weatherapp.utils.*
import com.shrikanthravi.collapsiblecalendarview.data.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForecastViewModel (
    private val repository: WeatherRepository,
    application: Application
): AndroidViewModel(application) {
    val list = arrayListOf<WeatherForecast>()
    val mutableList=MutableLiveData<List<WeatherForecast>?>()
    private val _noForecastText=MutableLiveData<String>()
    val noForecastText=_noForecastText.asLiveData()
    private val _weatherForecast = MutableLiveData<List<WeatherForecast>?>()
    val weatherForecast = _weatherForecast.asLiveData()
    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading = _isLoading.asLiveData()
    var firstTime = true
    lateinit var month: String
    lateinit var dateDay: String
    private val _isResetList=MutableLiveData<Boolean>()
    val isResetList=_isResetList.asLiveData()
    private val _dataFetchState = MutableLiveData<Boolean>()
    val dataFetchState = _dataFetchState.asLiveData()
    val sPref = PreferenceManager.getDefaultSharedPreferences(getApplication())
    fun getForecastWeather() {
        _isLoading.postValue(true)
        GlobalScope.launch(Dispatchers.Main) {
            val cityId = sPref.getInt(CITY_ID, 0)
            when (val result = repository.getForecastWeather(cityId, false)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        val forecast = result.data
                        Log.d("Weather",forecast.toString())
                        _dataFetchState.value = true
                        _weatherForecast.value = forecast
                    } else {
                        refreshWeatherForecast()
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

    fun onDaySelected(day: Day){
        if (firstTime) {
            firstTime = false
        }
        month = if (day!!.month + 1 < 10) {
            "0${day.month + 1}"
        } else {
            "${day.month + 1}"
        }
        dateDay = if (day.day < 10) {
            "0${day.day}"
        } else {
            "${day.day}"
        }
        val dateFromCalender = "${day.year}-$month-$dateDay"
        _noForecastText.value="No forecast available for $dateDay/$month/${day.year}"
        list.clear()
        for (element in _weatherForecast.value!!) {
            val date = element.date.substringBefore(" ", " ")
            if (date == dateFromCalender) {
                list.add(element)
            }
        }
    }
    fun populateOriginalList(){
        list.clear()
        list.addAll(weatherForecast.value!!)
    }
    fun addOriginalList(){
        list.clear()
        list.addAll(weatherForecast.value!!)
    }
    fun refreshWeatherForecast() {
        Log.d("Here","Here")
        _isLoading.value = true
        val cityId = sPref.getInt(CITY_ID, 0)
        viewModelScope.launch {
            when (val result = repository.getForecastWeather(cityId, true)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        val forecast = result.data
                        _dataFetchState.value = true
                        _weatherForecast.value = forecast
                        repository.deleteForecastData()
                        repository.storeForecastData(forecast)
                    } else {
                        _weatherForecast.postValue(null)
                        _dataFetchState.postValue(false)
                    }
                }
                is Error -> {
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
    class ForecastFragmentViewModelFactory(
        private val repository: WeatherRepository,
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            (ForecastViewModel(repository, application) as T)
    }
}