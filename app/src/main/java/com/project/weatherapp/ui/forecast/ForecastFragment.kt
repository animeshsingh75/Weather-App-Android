package com.project.weatherapp.ui.forecast

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.weatherapp.WeatherApplication
import com.project.weatherapp.data.model.WeatherForecast
import com.project.weatherapp.databinding.ForecastFragmentBinding
import com.project.weatherapp.ui.forecast.adapter.ForecastAdapter
import com.project.weatherapp.utils.isNetworkConnected
import com.project.weatherapp.utils.observeOnce
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.view.OnSwipeTouchListener
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


const val CITY_ID = "cityId"

class ForecastFragment : Fragment() {
    lateinit var binding: ForecastFragmentBinding
    private val viewModel by viewModels<ForecastViewModel> {
        ForecastViewModel.ForecastFragmentViewModelFactory(
            (requireContext().applicationContext as WeatherApplication).weatherRepository,
            requireActivity().application
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val adapter = ForecastAdapter(viewModel.list)
        binding = ForecastFragmentBinding.inflate(layoutInflater)
        if (isNetworkConnected(requireContext())) {
            viewModel.refreshWeatherForecast()
        } else {
            viewModel.getForecastWeather()
        }
        binding.swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToLoad.isRefreshing = false
                if (isNetworkConnected(requireActivity())) {
                    viewModel.refreshWeatherForecast()
                    binding.swipeToLoad.isRefreshing = false
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
        binding.collapsibleCalendarView.setExpandIconVisible(true)
        val today = GregorianCalendar()
        binding.collapsibleCalendarView.addEventTag(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        today.add(Calendar.DATE, 1)
        binding.collapsibleCalendarView.selectedDay = Day(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        binding.collapsibleCalendarView.resetList().observe(
            viewLifecycleOwner,
            {
                if (it) {
                    binding.forecastRv.isVisible = true
                    binding.noForecast.isVisible = false
                    viewModel.addOriginalList()
                    adapter.notifyDataSetChanged()
                }
            }
        )
        binding.collapsibleCalendarView.params = CollapsibleCalendar.Params(0, 6)
        binding.collapsibleCalendarView.setCalendarListener(object :
            CollapsibleCalendar.CalendarListener {
            override fun onDayChanged() {

            }

            override fun onClickListener() {
                if (binding.collapsibleCalendarView.expanded) {
                    binding.collapsibleCalendarView.collapse(400)
                } else {
                    binding.collapsibleCalendarView.expand(400)
                }
            }

            override fun onDaySelect() {
                viewModel.onDaySelected(binding.collapsibleCalendarView.selectedDay!!)
                if (viewModel.list.size == 0) {
                    binding.forecastRv.isVisible = false
                    binding.noForecast.isVisible = true
                    binding.noForecast.text = viewModel.noForecastText.value
                } else {
                    binding.forecastRv.isVisible = true
                    binding.noForecast.isVisible = false
                }
                adapter.notifyDataSetChanged()
            }

            override fun onItemClick(v: View) {

            }

            override fun onDataUpdate() {

            }

            override fun onMonthChange() {

            }

            override fun onWeekChange(position: Int) {

            }
        })
        viewModel.weatherForecast.observe(
            viewLifecycleOwner,
            {
                viewModel.populateOriginalList()
                adapter.notifyDataSetChanged()
            }
        )

        binding.forecastRv.layoutManager = LinearLayoutManager(activity)
        binding.forecastRv.adapter = adapter
        binding.forecastRv.isNestedScrollingEnabled = false
        return binding.root
    }
}