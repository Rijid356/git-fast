package com.gitfast.app.ui.dogwalk

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gitfast.app.R
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.EventCategory
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.ui.theme.NeonGreen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun EventRouteMap(
    gpsPoints: List<GpsPoint>,
    events: List<DogWalkEvent>,
    modifier: Modifier = Modifier,
) {
    if (gpsPoints.size < 2) return

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    val latLngPoints = remember(gpsPoints) {
        gpsPoints.map { LatLng(it.latitude, it.longitude) }
    }

    val mapStyleOptions = remember {
        try {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } catch (e: Exception) {
            Log.e("EventRouteMap", "Failed to load map style", e)
            null
        }
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            scrollGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        )
    }

    val mapProperties = remember(mapStyleOptions) {
        MapProperties(
            mapStyleOptions = mapStyleOptions ?: MapStyleOptions("[]"),
        )
    }

    LaunchedEffect(latLngPoints) {
        val boundsBuilder = LatLngBounds.builder()
        latLngPoints.forEach { boundsBuilder.include(it) }
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 64),
        )
    }

    var mapLoaded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
                properties = mapProperties,
                onMapLoaded = { mapLoaded = true },
            ) {
                // Route polyline
                Polyline(
                    points = latLngPoints,
                    color = NeonGreen,
                    width = 10f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                )

                // Event markers
                events.forEach { event ->
                    if (event.latitude != null && event.longitude != null) {
                        val hue = when (event.eventType.category) {
                            EventCategory.FORAGING -> BitmapDescriptorFactory.HUE_GREEN
                            EventCategory.BATHROOM -> BitmapDescriptorFactory.HUE_ORANGE
                            EventCategory.ENERGY -> BitmapDescriptorFactory.HUE_YELLOW
                            EventCategory.SOCIAL -> BitmapDescriptorFactory.HUE_AZURE
                        }
                        Marker(
                            state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = event.eventType.displayName,
                            icon = BitmapDescriptorFactory.defaultMarker(hue),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !mapLoaded,
                exit = fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {}
            }
        }
    }
}
