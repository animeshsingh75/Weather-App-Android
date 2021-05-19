package com.project.weatherapp.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.*
import com.google.android.libraries.places.widget.Autocomplete
import com.project.weatherapp.R
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.LocationModel
import com.project.weatherapp.ui.home.HomeViewModel
import com.project.weatherapp.utils.isNetworkConnected

class SettingsFragment : PreferenceFragmentCompat() {
    private val AUTOCOMPLETE_REQUEST_CODE = 1000
    lateinit var sPref:SharedPreferences
    private val viewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.SettingsFragmentViewModelFactory(
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
                if (viewModel.clicked.value!!) {
                    val intent = it.build(requireContext())
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                    viewModel.doneGettingPlace()
                }
            }
        )
        val useDeviceLocation = findPreference<SwitchPreference>("USE_DEVICE_LOCATION")
        val getCurrentLocationPreference = sPref.getBoolean(IS_CURRENT_LOCATION, true)
        useDeviceLocation!!.isChecked = getCurrentLocationPreference
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        sPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val setLocation = findPreference<Preference>("CUSTOM_LOCATION")
        setLocation!!.setOnPreferenceClickListener {
            viewModel.onSearchCalled()
            return@setOnPreferenceClickListener true
        }
        val unitSelected=findPreference<ListPreference>("UNIT_SYSTEM")
        unitSelected!!.setDefaultValue(sPref.getString(UNIT_SELECTED,"Metric"))
        unitSelected.setOnPreferenceChangeListener { _, newValue ->
            if(newValue.toString()=="METRIC"){
                sPref.edit().putString(UNIT_SELECTED,"Metric").apply()
            }else{
                sPref.edit().putString(UNIT_SELECTED,"Imperial").apply()
            }
            true
        }
        val useDeviceLocation = findPreference<SwitchPreference>("USE_DEVICE_LOCATION")
        useDeviceLocation!!.setOnPreferenceClickListener {
            if (! isNetworkConnected(requireContext())) {
                useDeviceLocation.isChecked = true
                Toast.makeText(
                    requireContext(),
                    "No internet connection.Pls try again later!!",
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        }
        useDeviceLocation.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                Log.d("NetworkStatus", isNetworkConnected(requireContext()).toString())
                if (isNetworkConnected(requireContext())) {
                    val changedValue = newValue as Boolean
                    if (changedValue) {
                        Toast.makeText(
                            requireContext(),
                            "Please refresh your home and forecast tab to update results",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    viewModel.onLocationDisabled(changedValue)
                }
                return@OnPreferenceChangeListener true
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val locationModel = LocationModel(place.latLng!!.longitude, place.latLng!!.latitude)
                viewModel.getCoords(locationModel)
                Toast.makeText(
                    requireContext(),
                    "Please refresh your home screen and forecast tab to update results",
                    Toast.LENGTH_LONG
                ).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}