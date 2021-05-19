package com.project.weatherapp.ui.forecast.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.weathericonview.WeatherIconView
import com.project.weatherapp.R
import com.project.weatherapp.data.model.WeatherForecast
import com.project.weatherapp.ui.settings.UNIT_SELECTED
import com.project.weatherapp.utils.*

class ForecastAdapter(val data:List<WeatherForecast>): RecyclerView.Adapter<ForecastViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder =
        ForecastViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_forecast,parent,false))


    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int=data.size
}

class ForecastViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    fun bind(item:WeatherForecast)= with(itemView){
        val tempTv=findViewById<TextView>(R.id.tempTv)
        val tvWeatherDescription=findViewById<TextView>(R.id.tvWeatherDescription)
        val tvMain=findViewById<TextView>(R.id.tvMain)
        val tvHumidity=findViewById<TextView>(R.id.tvHumidity)
        val tvPressure=findViewById<TextView>(R.id.tvPressure)
        val tvWindSpeed=findViewById<TextView>(R.id.tvWindSpeed)
        val tvDate=findViewById<TextView>(R.id.tvDate)
        val weatherIcon=findViewById<WeatherIconView>(R.id.weatherIcon)
        val sPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sPref.getString(UNIT_SELECTED, "Metric") == "Metric") {
            tempTv.text = "${convertKelvinToCelsius(item.networkWeatherCondition.temp)}" + "\u2103"
            tvWindSpeed.text = "${convertToOneDecimal(item.wind.speed)} m/s"
        } else {
            tempTv.text =
                "${convertCelsiusToFahrenheit(convertKelvinToCelsius(item.networkWeatherCondition.temp))}" + "\u2109"
            tvWindSpeed.text = "${converttoMilesPerHour(item.wind.speed)} mph"
        }
        tvMain.text=item.networkWeatherDescription[0].main
        tvWeatherDescription.text= capitalizeEachLetter(item.networkWeatherDescription[0].description!!)
        tvHumidity.text = "${item.networkWeatherCondition.humidity.toInt()} %"
        tvPressure.text = "${item.networkWeatherCondition.pressure.toInt()} hPa"
        tvDate.text=item.date
        when (item.networkWeatherDescription[0].icon) {
            "01d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_day_sunny))
            }
            "01n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_night_clear))
            }
            "02d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_day_cloudy))
            }
            "02n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_night_alt_cloudy))
            }
            "03d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_cloud))
            }
            "03n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_cloud))
            }
            "04d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_cloudy))
            }
            "04n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_cloudy))
            }
            "09d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_showers))
            }
            "09n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_showers))
            }
            "10d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_day_rain_mix))
            }
            "10n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_night_rain_mix))
            }
            "11d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_thunderstorm))
            }
            "11n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_thunderstorm))
            }
            "13d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_snow))
            }
            "13n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_snow))
            }
            "50d" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_dust))
            }
            "50n" -> {
                weatherIcon.setIconResource(resources.getString(R.string.wi_dust))
            }
        }
    }

}
