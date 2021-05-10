package com.project.weatherapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.project.weatherapp.R
import com.project.weatherapp.databinding.ActivityMainBinding
import com.project.weatherapp.ui.forecast.ForecastFragment
import com.project.weatherapp.ui.home.HomeFragment
import com.project.weatherapp.ui.search.SearchFragment
import com.project.weatherapp.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val homeFragment = HomeFragment()
        val forecastFragment = ForecastFragment()
        val searchFragment = SearchFragment()
        val settingsFragment = SettingsFragment()
        setCurrentFragment(homeFragment)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.homeFragment->setCurrentFragment(homeFragment)
                R.id.forecastFragment->setCurrentFragment(forecastFragment)
                R.id.searchFragment->setCurrentFragment(searchFragment)
                R.id.settingsFragment->setCurrentFragment(settingsFragment)
            }
            true
        }
    }
    private fun setCurrentFragment(fragment:Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container,fragment)
            commit()
        }

}
