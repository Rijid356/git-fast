package com.gitfast.app.data.model

data class WeatherData(
    val tempF: Int,
    val condition: String,
    val iconCode: String,
    val windSpeedMph: Int,
    val humidity: Int,
) {
    fun toWeatherCondition(): WeatherCondition = when {
        condition.equals("Clear", ignoreCase = true) -> WeatherCondition.SUNNY
        condition.equals("Clouds", ignoreCase = true) -> WeatherCondition.CLOUDY
        condition in listOf("Rain", "Drizzle", "Thunderstorm") -> WeatherCondition.RAINY
        condition.equals("Snow", ignoreCase = true) -> WeatherCondition.SNOWY
        windSpeedMph >= 20 -> WeatherCondition.WINDY
        else -> WeatherCondition.SUNNY
    }

    fun toWeatherTemp(): WeatherTemp = when {
        tempF > 85 -> WeatherTemp.HOT
        tempF > 70 -> WeatherTemp.WARM
        tempF > 55 -> WeatherTemp.MILD
        tempF > 40 -> WeatherTemp.COOL
        else -> WeatherTemp.COLD
    }
}
