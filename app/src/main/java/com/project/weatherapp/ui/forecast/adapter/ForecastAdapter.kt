package com.project.weatherapp.ui.forecast.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.weatherapp.R
import com.project.weatherapp.data.model.WeatherForecast

class ForecastAdapter(val data:List<WeatherForecast>): RecyclerView.Adapter<ForecastViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder =
        ForecastViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_forecast,parent,false))


    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int=data.size
}

class ForecastViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    fun bind(item:WeatherForecast)= with(itemView){
        val tempTv=findViewById<TextView>(R.id.tempTv)
        tempTv.text=item.date
    }

}
