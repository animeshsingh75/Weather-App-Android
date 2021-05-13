package com.project.weatherapp.ui.search

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.widget.Autocomplete
import com.project.weatherapp.R
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.databinding.SearchFragmentBinding
import com.project.weatherapp.ui.settings.UNIT_SELECTED
import com.project.weatherapp.utils.convertCelsiusToFahrenheit
import com.project.weatherapp.utils.converttoMilesPerHour
import com.project.weatherapp.utils.isNetworkConnected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {
    private val AUTOCOMPLETE_REQUEST_CODE = 1000
    lateinit var binding: SearchFragmentBinding
    private val viewModel by viewModels<SearchViewModel> {
        SearchViewModel.SearchFragmentViewModelFactory(
            (requireContext().applicationContext as WeatherApplication).weatherRepository,
            requireActivity().application
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        if (!isNetworkConnected(requireContext())) {
            binding.initialMessage.isVisible = false
            binding.noInternetMessage.isVisible = true
        }
        viewModel.autocompleteIntent.observe(
            viewLifecycleOwner,
            {
                if (viewModel.clicked.value!!) {
                    val intent = it.build(requireContext())
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                    viewModel.doneGettingPlace()
                }
            }
        )
        viewModel.lastUpdatedTime.observe(
            viewLifecycleOwner,
            { time ->
                binding.tvCurrentTime.text = time
            }
        )
        binding.swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToLoad.isRefreshing = false
                if(isNetworkConnected(requireActivity())){
                    viewModel.getSearchWeather()
                    viewModel.doneRefreshing()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No internet connection.Pls try again later!!",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.swipeToLoad.isRefreshing = false
                }
            }
        }
        viewModel.weather.observe(
            viewLifecycleOwner,
            { weather ->
                binding.noInternetMessage.isVisible=false
                binding.tvPlace.text = weather!!.name
                if(viewModel.sPref.getString(UNIT_SELECTED,"Metric")=="Metric"){
                    binding.tvTemp.text = "${weather.networkWeatherCondition.temp}" + "\u2103"
                    binding.tvWindSpeed.text = "${weather.wind.speed} m/s"
                }
                else{
                    binding.tvTemp.text = "${convertCelsiusToFahrenheit(weather.networkWeatherCondition.temp)}" + "\u2109"
                    binding.tvWindSpeed.text = "${converttoMilesPerHour(weather.wind.speed)} mph"
                }
                binding.tvWeatherDescription.text = weather.networkWeatherDescription[0].main
                binding.tvHumidity.text = "${weather.networkWeatherCondition.humidity}%"
                binding.tvPressure.text =
                    "${weather.networkWeatherCondition.pressure.toInt()}hPa"
                when (weather.networkWeatherDescription[0].icon) {
                    "01d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_day_sunny))
                    }
                    "01n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_night_clear))
                    }
                    "02d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_day_cloudy))
                    }
                    "02n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_night_alt_cloudy))
                    }
                    "03d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_cloud))
                    }
                    "03n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_cloud))
                    }
                    "04d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_cloudy))
                    }
                    "04n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_cloudy))
                    }
                    "09d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_showers))
                    }
                    "09n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_showers))
                    }
                    "10d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_day_rain_mix))
                    }
                    "10n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_night_rain_mix))
                    }
                    "11d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_thunderstorm))
                    }
                    "11n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_thunderstorm))
                    }
                    "13d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_snow))
                    }
                    "13n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_snow))
                    }
                    "50d" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_dust))
                    }
                    "50n" -> {
                        binding.weatherIcon.setIconResource(getString(R.string.wi_dust))
                    }
                }
            }
        )
        viewModel.locationModel.observe(
            viewLifecycleOwner,
            {
                viewModel.getSearchWeather()
            }
        )
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                binding.initialMessage.isVisible = false
                binding.swipeToLoad.isVisible = true
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val locationModel = LocationModel(place.latLng!!.longitude, place.latLng!!.latitude)
                viewModel.getCoords(locationModel)
            } else if (resultCode == RESULT_CANCELED) {
                binding.swipeToLoad.isVisible=false
                binding.initialMessage.isVisible=true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isNetworkConnected(requireContext())) {
            when (item.itemId) {
                R.id.search -> {
                    viewModel.onSearchCalled()
                    return true
                }
                else -> {
                    return false
                }
            }
        } else {
            when (item.itemId) {
                R.id.search -> {
                    Toast.makeText(
                        requireContext(),
                        "Pls connect to internet to search for cities",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return false
        }
    }
}