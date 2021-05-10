package com.project.weatherapp

import android.content.Context
import androidx.room.Room
import com.project.weatherapp.data.source.local.WeatherDatabase
import com.project.weatherapp.data.source.local.WeatherLocalDataSource
import com.project.weatherapp.data.source.local.WeatherLocalDataSourceImpl
import com.project.weatherapp.data.source.remote.WeatherRemoteDataSourceImpl
import com.project.weatherapp.data.source.repository.WeatherRepository
import com.project.weatherapp.data.source.repository.WeatherRepositoryImpl

object ServiceLocator {

    private var database: WeatherDatabase? = null

    @Volatile
    var weatherRepository: WeatherRepository? = null

    fun provideWeatherRepository(context: Context): WeatherRepository {
        synchronized(this) {
            return weatherRepository ?: createWeatherRepository(context)
        }
    }

    private fun createWeatherRepository(context: Context): WeatherRepository {
        val newRepo = WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl(),
            createLocalWeatherSource(context)
        )
        weatherRepository = newRepo
        return newRepo
    }

    private fun createLocalWeatherSource(context: Context): WeatherLocalDataSource {
        val database = database ?: createDatabase(context)
        return WeatherLocalDataSourceImpl(database.weatherDao)
    }

    private fun createDatabase(context: Context): WeatherDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            WeatherDatabase::class.java,
            "Weather.db"
        ).build()
        database = result
        return result
    }
}