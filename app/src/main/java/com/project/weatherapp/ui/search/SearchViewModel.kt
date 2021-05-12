package com.project.weatherapp.ui.search

import android.app.Application
import androidx.lifecycle.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.model.Weather
import com.project.weatherapp.data.source.remote.retrofit.PlaceSuggestionClient
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.utils.Result
import com.project.weatherapp.utils.asLiveData
import com.project.weatherapp.utils.convertKelvinToCelsius
import com.project.weatherapp.utils.currentSystemTime
import kotlinx.coroutines.launch

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
    private val _locationModel=MutableLiveData<LocationModel>()
    val locationModel=_locationModel.asLiveData()
    private val _weather = MutableLiveData<Weather?>()
    val weather = _weather.asLiveData()
    private var _lastUpdatedTime = MutableLiveData<String>()
    val lastUpdatedTime = _lastUpdatedTime.asLiveData()
    private fun getClient() {
        PlaceSuggestionClient.createPlaceClient(getApplication<WeatherApplication>())
    }

    fun onSearchCalled() {
        getClient()
        val fields = listOf(Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fields
        ).setTypeFilter(TypeFilter.CITIES)

        _autoCompleteIntent.value = intent
        _autoCompleteIntent.value = intent
    }
    fun getCoords(locationModel: LocationModel){
        _locationModel.value=locationModel
    }
    fun getSearchWeather() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getWeather(_locationModel.value!!,true)) {
                is Result.Success -> {
                    _isLoading.value = false
                    if (result.data != null) {
                        _lastUpdatedTime.value=currentSystemTime()
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
                is Result.Error -> TODO()
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


