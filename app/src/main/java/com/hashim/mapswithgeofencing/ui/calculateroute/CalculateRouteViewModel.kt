/*
 * Copyright (c) 2021/  3/ 28.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.ui.calculateroute

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hashim.mapswithgeofencing.Domain.model.Directions
import com.hashim.mapswithgeofencing.R
import com.hashim.mapswithgeofencing.prefrences.HlatLng
import com.hashim.mapswithgeofencing.prefrences.PrefTypes.*
import com.hashim.mapswithgeofencing.prefrences.SettingsPrefrences
import com.hashim.mapswithgeofencing.repository.remote.RemoteRepo
import com.hashim.mapswithgeofencing.ui.events.CalculateRouteStateEvent
import com.hashim.mapswithgeofencing.ui.events.CalculateRouteStateEvent.*
import com.hashim.mapswithgeofencing.ui.events.CalculateRouteViewState
import com.hashim.mapswithgeofencing.ui.events.CalculateRouteViewState.*
import com.hashim.mapswithgeofencing.ui.main.Category
import com.hashim.mapswithgeofencing.utils.DataState
import com.hashim.mapswithgeofencing.utils.MarkerUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculateRouteViewModel @Inject constructor(
        private val hRemoteRepo: RemoteRepo,
        private val hSettingsPrefrences: SettingsPrefrences,
        @ApplicationContext private val hContext: Context,
) : ViewModel() {

    private val _CalculateRouteStateEvent = MutableLiveData<CalculateRouteStateEvent>()

    private val _hCalculateRouteViewState = MutableLiveData<CalculateRouteViewState>()

    val hCalculateRouteViewState: LiveData<CalculateRouteViewState>
        get() = _hCalculateRouteViewState


    val hDataState: LiveData<DataState<CalculateRouteViewState>> = Transformations
            .switchMap(_CalculateRouteStateEvent) {
                it?.let { calculateRouteViewState ->
                    hHandleStateEvent(calculateRouteViewState)
                }
            }


    private fun hHandleStateEvent(stateEvent: CalculateRouteStateEvent): LiveData<DataState<CalculateRouteViewState>>? {
        when (stateEvent) {
            is OnFindDirections -> {
                viewModelScope.launch {
                    var hCurrentLocation = Location(LocationManager.GPS_PROVIDER)
                    if (stateEvent.hStartLocation == null) {
                        val hCurrentHlatLng: HlatLng = hSettingsPrefrences.hGetSettings(CURRENT_LAT_LNG_PT) as HlatLng
                        hCurrentLocation = Location(LocationManager.GPS_PROVIDER).apply {
                            latitude = hCurrentHlatLng.hLat!!
                            longitude = hCurrentHlatLng.hLng!!
                        }
                    }
                    val hDirections = hRemoteRepo.hGetDirections(
                            startLocation = hCurrentLocation,
                            endLocation = stateEvent.hDestinationLocation,
                            mode = stateEvent.hMode
                    )
                    hDrawPath(hDirections)
                }

            }
            is OnMapReady -> {
                hSetCurrentLocation()

            }
            is None -> {
            }
            else -> {
            }
        }
        return null
    }

    private fun hSetCurrentLocation() {
        val hCurrentHlatLng: HlatLng = hSettingsPrefrences.hGetSettings(CURRENT_LAT_LNG_PT) as HlatLng
        val hMapType: Int? = hSettingsPrefrences.hGetSettings(MAPS_TYPE_PT) as Int?
        val hCurrentLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = hCurrentHlatLng.hLat!!
            longitude = hCurrentHlatLng.hLng!!
        }
        _hCalculateRouteViewState.value = CalculateRouteViewState(
                hCalculateRouteFields = CalculateRouteFields(
                        hSetMapVS = SetMapVS(
                                currentLocation = hCurrentLocation,
                                cameraZoom = 12.0F,
                                hMapType = hMapType
                        )
                )
        )
    }



    private fun hDrawPath(hDirections: Directions) {

        val hDistance: String

        val hUnit = hSettingsPrefrences.hGetSettings(DISTANCE_UNIT_PT) as Int?
        hDistance = when (hUnit) {
            1 -> {
                hDirections.distance?.value?.div(1600).toString()
            }
            else -> {
                hDirections.distance?.text.toString()
            }
        }
        _hCalculateRouteViewState.value = CalculateRouteViewState(
                hCalculateRouteFields = CalculateRouteFields(
                        hDrawPathVS = DrawPathVS(
                                hDistance = hDirections.distance,
                                hOverviewPolyline = hDirections.overviewPolyline,
                                hSteps = hDirections.steps,
                                hDistanceUnit = hDistance,
                                hEta = String.format(hContext.getString(R.string.time), " ${hDirections.duration?.text}")
                        )
                )
        )

    }


    fun hSetStateEvent(calculateRouteStateEvent: CalculateRouteStateEvent) {
        _CalculateRouteStateEvent.value = calculateRouteStateEvent
    }


    fun hSetDrawPathVs(drawPathVS: DrawPathVS) {
        val hUpdate = hGetCurrentViewStateOrNew()
        hUpdate.hCalculateRouteFields.hDrawPathVS = drawPathVS
        _hCalculateRouteViewState.value = hUpdate
    }

    private fun hGetCurrentViewStateOrNew(): CalculateRouteViewState {
        return hCalculateRouteViewState.value ?: CalculateRouteViewState()
    }

    fun hSetCurrentLocationVs(setMapVS: SetMapVS) {
        val hUpdate = hGetCurrentViewStateOrNew()
        hUpdate.hCalculateRouteFields.hSetMapVS = setMapVS
        _hCalculateRouteViewState.value = hUpdate
    }

}