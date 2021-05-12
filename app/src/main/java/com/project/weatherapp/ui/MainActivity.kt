package com.project.weatherapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var sPref:SharedPreferences
    var allowed: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sPref = PreferenceManager.getDefaultSharedPreferences(application)
        val homeFragment = HomeFragment()
        val forecastFragment = ForecastFragment()
        val searchFragment = SearchFragment()
        val settingsFragment = SettingsFragment()
        setCurrentFragment(homeFragment)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            allowed = if (sPref.getBoolean(IS_CURRENT_LOCATION, true)) {
                true
            }else{
                sPref.getBoolean(IS_LOCATION_SET,true)
            }
            if(allowed) {
                when (it.itemId) {
                    R.id.homeFragment -> setCurrentFragment(homeFragment)
                    R.id.forecastFragment -> setCurrentFragment(forecastFragment)
                    R.id.searchFragment -> setCurrentFragment(searchFragment)
                    R.id.settingsFragment -> setCurrentFragment(settingsFragment)
                }
                true
            }
            else{
                Toast.makeText(this,"Pls select city before navigating",Toast.LENGTH_SHORT).show()
                return@setOnNavigationItemSelectedListener false
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }

    override fun onStop() {
        val getCurrentLocationPreference=sPref.getBoolean(IS_CURRENT_LOCATION,true)
        val getSetLocation=sPref.getBoolean(IS_LOCATION_SET,true)
        if(!getCurrentLocationPreference){
            if(!getSetLocation){
                sPref.edit().putBoolean(IS_CURRENT_LOCATION,true).apply()
                sPref.edit().putBoolean(IS_LOCATION_SET,true).apply()
            }
        }
        super.onStop()
    }
}
