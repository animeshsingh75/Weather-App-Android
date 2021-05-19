package com.project.weatherapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.project.weatherapp.R
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.databinding.HomeFragmentBinding
import com.project.weatherapp.ui.LOCATION_REQUEST_CODE
import com.project.weatherapp.ui.settings.UNIT_SELECTED
import com.project.weatherapp.utils.*
import kotlinx.coroutines.*


class HomeFragment : Fragment() {

    private var isGPSEnabled = false
    lateinit var binding: HomeFragmentBinding
    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModel.HomeFragmentViewModelFactory(
            (requireContext().applicationContext as WeatherApplication).weatherRepository,
            requireActivity().application
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GpsUtil(requireContext()).turnGPSOn(object : GpsUtil.OnGpsListener {
            override fun gpsStatus(isGPSEnabled: Boolean) {
                this@HomeFragment.isGPSEnabled = isGPSEnabled
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(layoutInflater)
        viewModel.weather.observe(
            viewLifecycleOwner,
            { weather ->
                binding.loadScreen.isVisible = false
                binding.swipeToLoad.isVisible = true
                binding.tvPlace.text = weather!!.name
                if (viewModel.sPref.getString(UNIT_SELECTED, "Metric") == "Metric") {
                    binding.tvTemp.text = "${weather.networkWeatherCondition.temp}" + "\u2103"
                    binding.tvWindSpeed.text = "${weather.wind.speed} m/s"
                } else {
                    binding.tvTemp.text =
                        "${convertCelsiusToFahrenheit(weather.networkWeatherCondition.temp)}" + "\u2109"
                    binding.tvWindSpeed.text = "${converttoMilesPerHour(weather.wind.speed)} mph"
                }
                binding.tvWeatherDescription.text = weather.networkWeatherDescription[0].main
                binding.tvHumidity.text = "${weather.networkWeatherCondition.humidity}%"
                binding.tvPressure.text = "${weather.networkWeatherCondition.pressure.toInt()}hPa"

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
                if (isNetworkConnected(requireActivity())) {
                    viewModel.getLocationLiveData().observeOnce(
                        viewLifecycleOwner,
                        {
                            viewModel.refreshWeather(it)
                            binding.swipeToLoad.isRefreshing = false
                        }
                    )
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
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    private fun invokeLocationAction() {
        when {
            allPermissionsGranted() -> {
                if (!isNetworkConnected(requireContext())) {
                    val sPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    if (sPref.getString(LAST_UPDATED_TIME, "") == "") {
                        AlertDialog.Builder(requireContext())
                            .setTitle("No network")
                            .setMessage("No offline weather is stored for the first time the app is launched")
                            .setNegativeButton(
                                "Ok "
                            ) { dialog, which -> requireActivity().finishAffinity() }
                            .show()
                    }
                }
                viewModel.getLocationLiveData().observeOnce(
                    viewLifecycleOwner,
                    { location ->
                        if (location != null) {
                            if (isNetworkConnected(requireContext())) {
                                viewModel.refreshWeather(location)
                                viewModel.doneRefreshing()
                            } else {
                                viewModel.getWeather(location)
                            }
                        }
                    }
                )
            }
            shouldShowRequestPermissionRationale() -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("This application requires access to your location to function!")
                    .setNegativeButton(
                        "No"
                    ) { _, _ -> requireActivity().finishAffinity() }
                    .setPositiveButton(
                        "Ask me"
                    ) { _, _ ->
                        requestPermissions(REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE)
                    }
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        isGPSEnabled = true
                        if (isNetworkConnected(requireContext())) {
                            binding.loadScreen.isVisible = true
                        }
                        invokeLocationAction()
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(
                            requireContext(),
                            "Enable your GPS and restart!",
                            Toast.LENGTH_LONG
                        ).show()
                        requireActivity().finishAffinity()
                    }
                }

            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale() = REQUIRED_PERMISSIONS.all {
        shouldShowRequestPermissionRationale(it)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            invokeLocationAction()
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


}