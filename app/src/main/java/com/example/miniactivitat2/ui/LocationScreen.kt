package com.example.miniactivitat2.ui

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.miniactivitat2.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LocationScreen(
    locationViewModel: LocationViewModel,
    mFusedClient: FusedLocationProviderClient,
    mSettingsClient: SettingsClient,
    context: MainActivity
) {
    val locationUiState by locationViewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Start Updates", modifier = Modifier.clickable {
                    if (!locationUiState.isStarted) {
                        locationViewModel.updateButtonColor()
                        locationViewModel.startUpdates(mFusedClient, mSettingsClient, context)
                        locationViewModel.getLastLocation(mFusedClient)
                    }
                }, color = if (locationUiState.isStarted) Color.Gray else Color.Red
            )
            Text(
                text = "Stop Updates", modifier = Modifier.clickable {
                    if (locationUiState.isStarted) {
                        locationViewModel.updateButtonColor()
                        locationViewModel.stopUpdates(mFusedClient)
                    }
                }, color = if (locationUiState.isStarted) Color.Red else Color.Gray
            )
        }

        Text(
            text = String.format(
                Locale.ENGLISH, "Latitude: %f", locationUiState.latitude
            )
        )
        Text(
            text = String.format(
                Locale.ENGLISH, "Longitude: %f", locationUiState.longitude
            )
        )
        Text(
            text = String.format(
                Locale.ENGLISH, "Last update: %s", locationUiState.lastUpdate
            )
        )
    }
}