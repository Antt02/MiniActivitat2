package com.example.miniactivitat2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.miniactivitat2.ui.LocationScreen
import com.example.miniactivitat2.ui.theme.MiniActivitat2Theme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniActivitat2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val mFusedClient = LocationServices.getFusedLocationProviderClient(this)
                    val mSettingsClient = LocationServices.getSettingsClient(this)
                    LocationScreen(
                        mFusedClient = mFusedClient,
                        mSettingsClient = mSettingsClient,
                        context = this
                    )
                }
            }
        }
    }
}