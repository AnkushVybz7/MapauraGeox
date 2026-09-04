package com.example.providers

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Implementation of MapProvider using OSMDroid.
 */
class OSMDroidMapProvider : MapProvider {
    
    @Composable
    override fun MapLayer(
        modifier: Modifier,
        satellite: Boolean,
        locationEnabled: Boolean,
        zoomLevel: Double,
        centerLat: Double,
        centerLon: Double
    ) {
        val context = LocalContext.current
        
        // Initialize OSMDroid configuration
        val osmdroidConfig = remember {
            Configuration.getInstance().apply {
                load(context, PreferenceManager.getDefaultSharedPreferences(context))
                userAgentValue = context.packageName
            }
        }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(if (satellite) TileSourceFactory.MAPNIK else TileSourceFactory.MAPNIK) // Both Mapnik for now, but conceptual diff
                    
                    controller.setZoom(zoomLevel)
                    controller.setCenter(GeoPoint(centerLat, centerLon))
                    setMultiTouchControls(true)
                    
                    if (locationEnabled) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                    }
                }
            },
            modifier = modifier.fillMaxSize(),
            update = { mapView ->
                // Update map logic if state changes
                if (locationEnabled) {
                    val overlay = mapView.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
                    if (overlay == null) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(mapView.context), mapView)
                        locationOverlay.enableMyLocation()
                        mapView.overlays.add(locationOverlay)
                    }
                } else {
                    mapView.overlays.removeAll { it is MyLocationNewOverlay }
                }
                
                // Update center and zoom if they changed programmatically
                if (mapView.zoomLevelDouble != zoomLevel) {
                    mapView.controller.setZoom(zoomLevel)
                }
                if (mapView.mapCenter.latitude != centerLat || mapView.mapCenter.longitude != centerLon) {
                    mapView.controller.animateTo(GeoPoint(centerLat, centerLon))
                }
            }
        )
    }
}
