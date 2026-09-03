package com.example.providers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Interface defining the modular Map Provider architecture.
 */
interface MapProvider {
    /**
     * Renders the map layer.
     */
    @Composable
    fun MapLayer(
        modifier: Modifier,
        satellite: Boolean,
        locationEnabled: Boolean,
        zoomLevel: Double,
        centerLat: Double,
        centerLon: Double
    )
}
