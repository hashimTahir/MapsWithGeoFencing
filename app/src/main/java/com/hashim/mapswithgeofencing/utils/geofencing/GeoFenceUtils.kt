/*
 * Copyright (c) 2021/  4/ 6.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.utils.geofencing

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber


class GeoFenceUtils(
        private val hContext: Context,
        private var hRadius: Int = 100,
) {
    private val hGeofencingClient: GeofencingClient = LocationServices.getGeofencingClient(hContext)
    private val hGeoList = mutableListOf<Geofence>()
    private var hGeofencePendingIntent: PendingIntent? = null
    lateinit var hLatLan: LatLng


    /*For Locations added by user create geofences*/
    fun hCreateAllGeoFences(hLocationMap: HashMap<String, LatLng>) {
        hLocationMap.forEach {
            hLatLan = it.value
            hGeoList.add(
                    hCreateGeoFence(
                            hLatLan = it.value,
                            hKey = it.key
                    )
            )
        }
        Timber.d("Geo fence create")
    }

    /*Start the moniting process*/
    @SuppressLint("MissingPermission")
    fun hStartService() {
        hGeofencingClient.addGeofences(
                hCreateGeoFenceRequest(),
                hCreateGeoPendingIntent()
        )
    }

    /*What to be executed ,When a Geofence is triggered */
    private fun hCreateGeoPendingIntent(): PendingIntent? {
        if (hGeofencePendingIntent != null)
            return hGeofencePendingIntent

        val intent = Intent(hContext, GeofenceBroadcastReceiver::class.java)

        hGeofencePendingIntent = PendingIntent.getBroadcast(
                hContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        return hGeofencePendingIntent
    }


    /*Request to create a geo visibe fence*/
    private fun hCreateGeoFenceRequest() = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(hGeoList)
            .build()


    /*Builder that creates the fence and adds it to the list*/
    private fun hCreateGeoFence(
            hLatLan: LatLng,
            hKey: String

    ): Geofence {
        return Geofence.Builder()
                .setRequestId(hKey)
                .setCircularRegion(
                        hLatLan.latitude,
                        hLatLan.longitude,
                        hRadius.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or
                                Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
    }

    /*Visible circle is drawn on the map to show the fence perimeter*/
    fun hShowVisibleGeoFence(): CircleOptions {
        Timber.d("LatLng $hLatLan")
        return CircleOptions()
                .strokeColor(Color.argb(50, 70, 6, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .center(hLatLan)
                .radius(hRadius.toDouble())
    }

}