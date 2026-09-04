package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val _zoomLevel = MutableStateFlow(15.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    private val _centerLat = MutableStateFlow(28.6139)
    val centerLat: StateFlow<Double> = _centerLat.asStateFlow()

    private val _centerLon = MutableStateFlow(77.2090)
    val centerLon: StateFlow<Double> = _centerLon.asStateFlow()

    private val _is3DMode = MutableStateFlow(false)
    val is3DMode: StateFlow<Boolean> = _is3DMode.asStateFlow()

    private val _layersVisible = MutableStateFlow(true)
    val layersVisible: StateFlow<Boolean> = _layersVisible.asStateFlow()

    fun setZoomLevel(zoom: Double) {
        _zoomLevel.value = zoom
    }

    fun setCenter(lat: Double, lon: Double) {
        _centerLat.value = lat
        _centerLon.value = lon
    }

    fun toggle3DMode() {
        _is3DMode.value = !_is3DMode.value
    }

    fun toggleLayers() {
        _layersVisible.value = !_layersVisible.value
    }
}
