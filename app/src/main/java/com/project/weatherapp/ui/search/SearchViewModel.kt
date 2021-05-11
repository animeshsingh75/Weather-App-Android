package com.project.weatherapp.ui.search

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.City
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.Weather
import com.project.weatherapp.data.source.remote.retrofit.PlaceSuggestionClient
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.ui.home.HomeViewModel
import com.project.weatherapp.ui.home.LAST_UPDATED_TIME
import com.project.weatherapp.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SearchViewModel(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _autoCompleteIntent = MutableLiveData<Autocomplete.IntentBuilder>()
    val autocompleteIntent = _autoCompleteIntent.asLiveData()
    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading = _isLoading.asLiveData()
    private val _dataFetchState = MutableLiveData<Boolean>()
    val dataFetchState = _dataFetchState.asLiveData()
    private val _locationModel=MutableLiveData<City>()
    val locationModel=_locationModel.asLiveData()
    private val _weather = MutableLiveData<Weather?>()
    val weather = _weather.asLiveData()
    private var _lastUpdatedTime = MutableLiveData<String>()
    val lastUpdatedTime = _lastUpdatedTime.asLiveData()
    fun getClient() {
        PlaceSuggestionClient.createPlaceClient(getApplication<WeatherApplication>())
    }

    fun onSearchCalled() {
        getClient()
        val fields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fields
        ).setTypeFilter(TypeFilter.CITIES)

        _autoCompleteIntent.value = intent
    }
    fun getCity(city:City){
        _locationModel.value=city
    }
    fun getSearchWeather() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getSearchWeather(_locationModel.value!!.name)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        _lastUpdatedTime.value=currentSystemTime()
                        Log.d("Weather",result.data.toString())
                        val weather = result.data.apply {
                            this.networkWeatherCondition.temp =
                                convertKelvinToCelsius(this.networkWeatherCondition.temp)
                        }
                        _dataFetchState.value = true
                        _weather.value = weather
                    } else {
                        _weather.postValue(null)
                        _dataFetchState.postValue(false)

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
    fun doneRefreshing() {
        _isLoading.value = null
    }

    class SearchFragmentViewModelFactory(
        private val repository: WeatherRepository,
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            (SearchViewModel(repository, application) as T)
    }
}


