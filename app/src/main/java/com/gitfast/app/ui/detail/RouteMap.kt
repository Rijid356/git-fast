package com.gitfast.app.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gitfast.app.R
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
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

@Composable
fun RouteMap(
    points: List<LatLngPoint>,
    bounds: RouteBounds?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    val latLngPoints = remember(points) {
        points.map { LatLng(it.latitude, it.longitude) }
    }

    val mapStyleOptions = remember {
        try {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } catch (e: Exception) {
            Log.e("RouteMap", "Failed to load map style", e)
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

    LaunchedEffect(bounds) {
        if (bounds != null) {
            val latLngBounds = LatLngBounds(
                LatLng(bounds.minLat, bounds.minLng),
                LatLng(bounds.maxLat, bounds.maxLng),
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, 64),
            )
        }
    }

    var mapLoaded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapLoaded = {
                Log.d("RouteMap", "Map tiles loaded successfully")
                mapLoaded = true
            },
        ) {
            if (latLngPoints.size >= 2) {
                Polyline(
                    points = latLngPoints,
                    color = NeonGreen,
                    width = 12f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                )

                Marker(
                    state = MarkerState(position = latLngPoints.first()),
                    title = "Start",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN,
                    ),
                )

                Marker(
                    state = MarkerState(position = latLngPoints.last()),
                    title = "End",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED,
                    ),
                )
            }
        }
        // Dark overlay that hides the white flash while map tiles load
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
