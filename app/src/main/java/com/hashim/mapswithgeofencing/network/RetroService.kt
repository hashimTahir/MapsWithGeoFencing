/*
 * Copyright (c) 2021/  3/ 20.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.network

import com.hashim.mapswithgeofencing.network.response.directions.DirectionsResponse
import com.hashim.mapswithgeofencing.network.response.forecast.ForecastDto
import com.hashim.mapswithgeofencing.network.response.nearybyplaces.NearByPlacesDto
import com.hashim.mapswithgeofencing.network.response.weather.WeatherDto
import com.hashim.mapswithgeofencing.utils.Constants.Companion.H_DIRECTIONS_URL
import com.hashim.mapswithgeofencing.utils.Constants.Companion.H_GET_FORECAST_URL
import com.hashim.mapswithgeofencing.utils.Constants.Companion.H_GET_WEATHER_URL
import com.hashim.mapswithgeofencing.utils.Constants.Companion.H_NEARBY_PLACES_URL
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroService {
    @GET(H_GET_WEATHER_URL)
    suspend fun hGetWeather(
            @Query("lat") lat: String,
            @Query("lon") lng: String,
            @Query("APPID") key: String,
            @Query("units") unit: String
    ): WeatherDto


    @GET(H_GET_FORECAST_URL)
    suspend fun hGetForecast(
            @Query("lat") lat: String,
            @Query("lon") lng: String,
            @Query("APPID") key: String,
            @Query("units") unit: String
    ): ForecastDto


    @GET(H_NEARBY_PLACES_URL)
    suspend fun hFindNearByPlaces(
            @Query("location") location: String,
            @Query("radius") radius: String,
            @Query("type") type: String,
            @Query("key") key: String,
    ): NearByPlacesDto

    @GET(H_DIRECTIONS_URL)
    suspend fun hFindDirections(
            @Query("origin") startLocation: String,
            @Query("destination") endLocation: String,
            @Query("key") key: String,
            @Query("mode") mode: String,
    ): DirectionsResponse
}