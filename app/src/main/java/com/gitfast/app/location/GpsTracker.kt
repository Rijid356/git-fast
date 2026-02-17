package com.gitfast.app.location

import android.content.Context
import android.os.Looper
import com.gitfast.app.data.model.GpsPoint
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class GpsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val UPDATE_INTERVAL_MS = 2_000L
        const val FASTEST_INTERVAL_MS = 1_000L
        const val MIN_DISPLACEMENT_METERS = 3f
        const val MAX_ACCURACY_METERS = 20f
    }

    @Suppress("MissingPermission")
    fun startTracking(): Flow<GpsPoint> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    if (location.accuracy <= MAX_ACCURACY_METERS) {
                        val point = GpsPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = Instant.ofEpochMilli(location.time),
                            accuracy = location.accuracy,
                            speed = if (location.hasSpeed()) location.speed else null
                        )
                        trySend(point)
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }
}
