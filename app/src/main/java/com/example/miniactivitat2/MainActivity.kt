package com.example.miniactivitat2

import android.Manifest
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.example.miniactivitat2.ui.LocationScreen
import com.example.miniactivitat2.ui.LocationViewModel
import com.example.miniactivitat2.ui.theme.MiniActivitat2Theme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mFusedClient = LocationServices.getFusedLocationProviderClient(this)
        val mSettingsClient = LocationServices.getSettingsClient(this)
        val locationViewModel = LocationViewModel()
        //hauria de ser un rememberLauncherForActivityResult però no funciona
        locationViewModel.locationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissions ->
                //locationViewModel.onPermissionResult(permissions, mFusedClient, mSettingsClient, snackbarHostState, this)
                when {
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        false
                    ) -> {
                        // Precise location access granted.
                        Log.i(
                            TAG,
                            "User agreed to make precise required location settings changes, updates requested, starting location updates."
                        )
                        locationViewModel.startUpdates(mFusedClient, mSettingsClient, this)
                        locationViewModel.updateButtonColor()
                        locationViewModel.getLastLocation(mFusedClient)
                    }

                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        false
                    ) -> {
                        // Only approximate location access granted.
                        Log.i(
                            TAG,
                            "User agreed to make coarse required location settings changes, updates requested, starting location updates."
                        )
                        locationViewModel.startUpdates(mFusedClient, mSettingsClient, this)
                        locationViewModel.updateButtonColor()
                        locationViewModel.getLastLocation(mFusedClient)
                    }
                }
            }
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            MiniActivitat2Theme {
                Scaffold(snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }) { paddingValues ->
                    Log.i(TAG, paddingValues.toString())
                    //Screen in itself
                    LocationScreen(
                        mFusedClient = mFusedClient,
                        mSettingsClient = mSettingsClient,
                        locationViewModel = locationViewModel,
                        context = this,
                    )
                }
            }

            if (!locationViewModel.checkPermissions(this) && !locationViewModel.settings) {
                locationViewModel.requestPermissions(this, snackbarHostState)
            }

        }
    }
}
