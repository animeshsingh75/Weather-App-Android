package com.project.weatherapp.data.source.remote.retrofit

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.project.weatherapp.BuildConfig

object PlaceSuggestionClient {

    fun createPlaceClient(context:Context){
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.CITY_SUGGESTION_API_KEY)
        }
    }
}