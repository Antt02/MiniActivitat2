package com.example.miniactivitat2.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
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
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class LocationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var mLocationCallback: LocationCallback

    lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var mRequestingLocationUpdates: Boolean = false
    var settings = false

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
        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
            _uiState.update { currentState ->
                currentState.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    lastUpdate = DateFormat.getTimeInstance().format(Date())
                )
            }
        }
    }

    @Composable
    fun requestPermissions(context: MainActivity, snackbarHostState: SnackbarHostState) {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
//            showSnackbar(
//                mainText = "Location needed for core functionality",
//                actionText = "OK",
//                snackbarHostState = snackbarHostState
//            ) { // Build intent that displays the App settings screen.
//                val intent = Intent()
//                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                val uri = Uri.fromParts(
//                    "package",
//                    //BuildConfig.APPLICATION_ID , null
//                    "packageName", null
//                )  // Amb la darrera API level deprecated. Ara és packageName
//                intent.data = uri
//                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(context, intent, null)
//            }

            if (checkPermissions(context)) {
                updateButtonColor()
            }
            settings = true
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun checkPermissions(context: MainActivity): Boolean {
        val permissionFineState = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseState = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return ((permissionFineState == PackageManager.PERMISSION_GRANTED) || (permissionCoarseState == PackageManager.PERMISSION_GRANTED))
    }

//    @SuppressLint("CoroutineCreationDuringComposition")
//    @Composable
//    fun showSnackbar(
//        mainText: String,
//        actionText: String,
//        snackbarHostState: SnackbarHostState,
//        function: () -> Unit
//    ) {
//        //val scope = rememberCoroutineScope()
//        scope.launch {
//            val snackbarResult = snackbarHostState.showSnackbar(
//                message = mainText,
//                actionLabel = actionText,
//            )
//            when (snackbarResult) {
//                SnackbarResult.Dismissed -> {
//                    Log.i(TAG, "SnackbarIgnored")
//                }
//
//                SnackbarResult.ActionPerformed -> {
//                    // Request permission
//                    locationPermissionLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        )
//                    )
//                }
//            }
//        }
//
//    }


    fun onPermissionResult(
        permission: Map<String, @JvmSuppressWildcards Boolean>,
        mFusedClient: FusedLocationProviderClient,
        mSettingsClient: SettingsClient,
        snackbarHostState: SnackbarHostState,
        context:MainActivity
    ) {
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.i(
                    TAG,
                    "User agreed to make precise required location settings changes, updates requested, starting location updates."
                )
                startUpdates(mFusedClient, mSettingsClient, context)
                updateButtonColor()
                getLastLocation(mFusedClient)
            }

            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.i(
                    TAG,
                    "User agreed to make coarse required location settings changes, updates requested, starting location updates."
                )
                startUpdates(mFusedClient, mSettingsClient, context)
                updateButtonColor()
                getLastLocation(mFusedClient)
            }

//            else -> {
//                // No location access granted.
//                showSnackbar(
//                    "Permission needed for core functionality",
//                    "Go to Settings",
//                    snackbarHostState
//                ) { // Build intent that displays the App settings screen.
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts("package", "packageName", null)
//                    intent.data = uri
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(context, intent, null)
//                }
//            }
        }
    }
}