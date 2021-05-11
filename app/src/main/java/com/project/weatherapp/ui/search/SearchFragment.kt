package com.project.weatherapp.ui.search

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.widget.Autocomplete
import com.project.weatherapp.R
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.City
import com.project.weatherapp.databinding.SearchFragmentBinding
import com.project.weatherapp.utils.isNetworkConnected
import com.project.weatherapp.utils.observeOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class SearchFragment : Fragment() {
    private val AUTOCOMPLETE_REQUEST_CODE = 100
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
                val intent = it.build(requireContext())
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
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
                viewModel.getSearchWeather()
                viewModel.doneRefreshing()
                binding.swipeToLoad.isRefreshing = false
            }
        }
        viewModel.weather.observe(
            viewLifecycleOwner,
            Observer
            { weather ->
                Log.d("Weather", weather.toString())
                if (weather != null) {
                    binding.tvPlace.text = weather!!.name
                    binding.tvTemp.text = "${weather.networkWeatherCondition.temp}" + "\u2103"
                    //+ " \u2109" for fahrenheit
                    binding.tvWeatherDescription.text = weather.networkWeatherDescription[0].main
                    binding.tvHumidity.text = "${weather.networkWeatherCondition.humidity}%"
                    binding.tvPressure.text =
                        "${weather.networkWeatherCondition.pressure.toInt()}hPa"
                    binding.tvWindSpeed.text = "${weather.wind.speed} m/s"
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
                } else {
                    binding.swipeToLoad.isVisible=false
                    binding.initialMessage.isVisible=true
                    Toast.makeText(requireContext(),"Sorry but OpenWeather API and Google Places dont have the same spelling",Toast.LENGTH_LONG).show()
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
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val address = place.address
                val country = address!!.substringAfterLast(",", " ")
                Log.d("Places", country)
                val city = City(place.name!!, country)
                viewModel.getCity(city)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
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
                    Log.d("Clicked", "Clicked")
                    binding.initialMessage.isVisible = false
                    binding.swipeToLoad.isVisible = true
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