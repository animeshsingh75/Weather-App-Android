package com.project.weatherapp.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.android.libraries.places.widget.Autocomplete
import com.project.weatherapp.R
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.ui.home.HomeViewModel

class SettingsFragment : PreferenceFragmentCompat() {
    private val AUTOCOMPLETE_REQUEST_CODE = 1000
    private val viewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.SettingsFragmentViewModelFactory(
            requireActivity().application
        )
    }
    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModel.HomeFragmentViewModelFactory(
            (requireContext().applicationContext as WeatherApplication).weatherRepository,
            requireActivity().application
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.autocompleteIntent.observe(
            viewLifecycleOwner,
            {
                if(viewModel.clicked.value!!){
                    Log.d("Build","Build")
                    val intent = it.build(requireContext())
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                    viewModel.doneGettingPlace()
                }
            }
        )
        val sPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val useDeviceLocation = findPreference<SwitchPreference>("USE_DEVICE_LOCATION")
        val getCurrentLocationPreference = sPref.getBoolean(IS_CURRENT_LOCATION, true)
        useDeviceLocation!!.isChecked = getCurrentLocationPreference
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val setLocation = findPreference<Preference>("CUSTOM_LOCATION")
        val sPref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setLocation!!.setOnPreferenceClickListener {
            viewModel.onSearchCalled()
            return@setOnPreferenceClickListener true
        }

        val useDeviceLocation = findPreference<SwitchPreference>("USE_DEVICE_LOCATION")
        useDeviceLocation!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val changedValue = newValue as Boolean
                if(changedValue){
                    Toast.makeText(requireContext(),"Please refresh your home screen to update results",Toast.LENGTH_LONG).show()
                }
                viewModel.onLocationDisabled(changedValue)
                return@OnPreferenceChangeListener true
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val locationModel = LocationModel(place.latLng!!.longitude, place.latLng!!.latitude)
                viewModel.getCoords(locationModel)
                homeViewModel.refreshWeather(locationModel)
                Toast.makeText(requireContext(),"Please refresh your home screen to update results",Toast.LENGTH_LONG).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}