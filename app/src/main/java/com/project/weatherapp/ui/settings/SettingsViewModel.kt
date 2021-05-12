package com.project.weatherapp.ui.settings

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.data.source.remote.retrofit.PlaceSuggestionClient
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.ui.home.HomeViewModel
import com.project.weatherapp.utils.asLiveData

const val IS_CURRENT_LOCATION = "isCurrentLocation"
const val IS_LOCATION_SET = "isLocationSet"
const val LOCATION_MODEL = "locationModel"
const val UNIT_SELECTED = "unitSelected"

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    val sPref = PreferenceManager.getDefaultSharedPreferences(getApplication())
    private val _autoCompleteIntent = MutableLiveData<Autocomplete.IntentBuilder>()
    val autocompleteIntent = _autoCompleteIntent.asLiveData()
    private val _locationModel = MutableLiveData<LocationModel>()
    val locationModel = _locationModel.asLiveData()
    private val _setCurrentLocation = MutableLiveData<Boolean>()
    val setCurrentLocation = _setCurrentLocation.asLiveData()
    private val _clicked = MutableLiveData<Boolean>()
    val clicked= _clicked.asLiveData()
    fun doneGettingPlace(){
        _clicked.value=false
    }
    private fun getClient() {
        PlaceSuggestionClient.createPlaceClient(getApplication<WeatherApplication>())
    }

    fun onLocationDisabled(newValue: Boolean) {
        if (newValue == false) {
            sPref.edit().putBoolean(IS_CURRENT_LOCATION, newValue).apply()
            sPref.edit().putBoolean(IS_LOCATION_SET, false).apply()
        } else {
            sPref.edit().putBoolean(IS_CURRENT_LOCATION, newValue).apply()
            sPref.edit().putBoolean(IS_LOCATION_SET, true).apply()
            sPref.edit().putString(LOCATION_MODEL,"").apply()
        }
    }

    fun onSearchCalled() {
        val currentLocation=sPref.getBoolean(IS_CURRENT_LOCATION,true)
        if(!currentLocation){
            getClient()
            val fields = listOf(Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            ).setTypeFilter(TypeFilter.CITIES)
            _clicked.value=true
            _autoCompleteIntent.value = intent
        }
    }

    fun getCoords(locationModel: LocationModel) {
        sPref.edit().putBoolean(IS_LOCATION_SET, true).apply()
        val gson = Gson()
        val json = gson.toJson(locationModel)
        sPref.edit().putString(LOCATION_MODEL, json).apply()
    }

    class SettingsFragmentViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            (SettingsViewModel(application) as T)
    }
}