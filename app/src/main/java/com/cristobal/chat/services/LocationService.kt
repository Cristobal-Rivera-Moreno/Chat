package com.cristobal.chat.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationService (context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLastLocation(onResult: (lat: Double, lon: Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    onResult(0.0, 0.0)
                }
            }
            .addOnFailureListener {
                onResult(0.0, 0.0)
            }
    }
}