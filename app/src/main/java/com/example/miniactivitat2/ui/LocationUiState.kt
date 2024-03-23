package com.example.miniactivitat2.ui

data class LocationUiState(
    var isStarted: Boolean = false,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var lastUpdate: String = "N/A",
)

