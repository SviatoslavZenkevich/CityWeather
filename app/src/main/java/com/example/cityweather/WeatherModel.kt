package com.example.cityweather

data class WeatherModel(
    val city: String,
    val date: String,
    val condition: String,
    val imageUrl: String,
    val currentTemp: String,
    val maxTemp:String
)

