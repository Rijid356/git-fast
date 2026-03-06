package com.gitfast.app.data.remote

data class WeatherResponse(
    val weather: List<WeatherInfo>,
    val main: MainInfo,
    val wind: WindInfo,
)

data class WeatherInfo(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

data class MainInfo(
    val temp: Double,
    val humidity: Int,
)

data class WindInfo(
    val speed: Double,
)
