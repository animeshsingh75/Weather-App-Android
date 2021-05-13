package com.project.weatherapp.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.project.weatherapp.R
import com.project.weatherapp.databinding.ActivityMainBinding
import com.project.weatherapp.ui.forecast.ForecastFragment
import com.project.weatherapp.ui.home.HomeFragment
import com.project.weatherapp.ui.search.SearchFragment
import com.project.weatherapp.ui.settings.IS_CURRENT_LOCATION
import com.project.weatherapp.ui.settings.IS_LOCATION_SET
import com.project.weatherapp.ui.settings.SettingsFragment

const val LOCATION_REQUEST_CODE = 123

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var sPref: SharedPreferences
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var allowed: Boolean = true
    lateinit var homeFragment: HomeFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions(REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE)
        sPref = PreferenceManager.getDefaultSharedPreferences(application)
        homeFragment = HomeFragment()
        val forecastFragment = ForecastFragment()
        val searchFragment = SearchFragment()
        val settingsFragment = SettingsFragment()
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            allowed = if (sPref.getBoolean(IS_CURRENT_LOCATION, true)) {
                true
            } else {
                sPref.getBoolean(IS_LOCATION_SET, true)
            }
            if (allowed) {
                when (it.itemId) {
                    R.id.homeFragment -> setCurrentFragment(homeFragment)
                    R.id.forecastFragment -> setCurrentFragment(forecastFragment)
                    R.id.searchFragment -> setCurrentFragment(searchFragment)
                    R.id.settingsFragment -> setCurrentFragment(settingsFragment)
                }
                true
            } else {
                Toast.makeText(this, "Pls select city before navigating", Toast.LENGTH_SHORT).show()
                return@setOnNavigationItemSelectedListener false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frag = homeFragment
        frag.onActivityResult(requestCode, resultCode, data)
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            invokePermission()
        }
    }
    private fun invokePermission(){
        when{
            allPermissionsGranted()->{
                setCurrentFragment(homeFragment)
            }
            shouldShowRequestPermissionRationale()->{
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("This application requires access to your location to function!")
                    .setNegativeButton(
                        "No"
                    ) { _, _ -> this.finishAffinity() }
                    .setPositiveButton(
                        "Ask me"
                    ) { _, _ ->
                        requestPermissions(REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE)
                    }
                    .show()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun shouldShowRequestPermissionRationale() = REQUIRED_PERMISSIONS.all {
        shouldShowRequestPermissionRationale(it)
    }
    override fun onStop() {
        val getCurrentLocationPreference = sPref.getBoolean(IS_CURRENT_LOCATION, true)
        val getSetLocation = sPref.getBoolean(IS_LOCATION_SET, true)
        if (!getCurrentLocationPreference) {
            if (!getSetLocation) {
                sPref.edit().putBoolean(IS_CURRENT_LOCATION, true).apply()
                sPref.edit().putBoolean(IS_LOCATION_SET, true).apply()
            }
        }
        super.onStop()
    }
}
