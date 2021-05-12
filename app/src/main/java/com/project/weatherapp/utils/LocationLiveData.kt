package com.project.weatherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.ui.home.HomeViewModel
import com.project.weatherapp.ui.settings.IS_CURRENT_LOCATION
import com.project.weatherapp.ui.settings.LOCATION_MODEL

class LocationLiveData(context: Context) : LiveData<LocationModel>() {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val sPref = PreferenceManager.getDefaultSharedPreferences(context)
    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    private fun setLocationData(location: Location) {
        val gson=Gson()
        val jsonRetrieve=sPref.getString(LOCATION_MODEL,"")
        val currentLocation=sPref.getBoolean(IS_CURRENT_LOCATION,true)
        val locationModelRetrieve=gson.fromJson(jsonRetrieve,LocationModel::class.java)
        if(jsonRetrieve!="" && !currentLocation){
            value= LocationModel(
                longitude = locationModelRetrieve.longitude,
                latitude = locationModelRetrieve.latitude
            )
        }else{
            value = LocationModel(
                longitude = location.longitude,
                latitude = location.latitude
            )
        }
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}