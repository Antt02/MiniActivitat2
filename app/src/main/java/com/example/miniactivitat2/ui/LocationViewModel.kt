package com.example.miniactivitat2.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.miniactivitat2.MainActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DateFormat
import java.util.Date

class LocationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var mLocationCallback: LocationCallback

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var mRequestingLocationUpdates: Boolean = false
    private var settings = false

    init {
        createLocationRequest()
        createLocationCallback()
        buildLocationSettingsRequest()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.Builder(50).setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(false).setMinUpdateIntervalMillis(50)
            .setMaxUpdateDelayMillis(100).build()
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            lastUpdate = DateFormat.getTimeInstance().format(Date())
                        )
                    }
                }
            }
        }
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    fun updateButtonColor() {
        _uiState.update { currentState ->
            currentState.copy(
                isStarted = !currentState.isStarted,
            )
        }
    }

    fun startUpdates(
        mFusedLocationClient: FusedLocationProviderClient,
        mSettingsClient: SettingsClient,
        context: MainActivity
    ) {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener {
            if ((ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback, Looper.myLooper()!!
            )
        }.addOnFailureListener(context) { e ->
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(context, 0x1)
                    } catch (sie: IntentSender.SendIntentException) {
                        Log.i(TAG, "PendingIntent unable to execute request.")
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    val errorMessage =
                        "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                    Log.e(TAG, errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    mRequestingLocationUpdates = false
                }
            }
        }
    }

    fun stopUpdates(mFusedLocationClient: FusedLocationProviderClient) {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(mFusedLocationClient: FusedLocationProviderClient) {
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                _uiState.update { currentState ->
                    currentState.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        lastUpdate = DateFormat.getTimeInstance().format(Date())
                    )
                }
            }
    }
}