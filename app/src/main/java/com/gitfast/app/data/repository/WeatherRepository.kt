package com.gitfast.app.data.repository

import com.gitfast.app.BuildConfig
import com.gitfast.app.data.model.WeatherData
import com.gitfast.app.data.remote.WeatherApiService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
) {

    suspend fun fetchWeather(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = weatherApiService.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.OPENWEATHER_API_KEY,
            )
            val weather = response.weather.firstOrNull()
            Result.success(
                WeatherData(
                    tempF = response.main.temp.toInt(),
                    condition = weather?.main ?: "Clear",
                    iconCode = weather?.icon ?: "01d",
                    windSpeedMph = response.wind.speed.toInt(),
                    humidity = response.main.humidity,
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch weather for lat=%f, lon=%f", lat, lon)
            Result.failure(e)
        }
    }
}
