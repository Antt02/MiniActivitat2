package com.example.miniactivitat2

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
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            locationViewModel.locationPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ){ permissions ->
                        locationViewModel.onPermissionResult(permissions, mFusedClient, mSettingsClient, snackbarHostState, this)
                }
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
