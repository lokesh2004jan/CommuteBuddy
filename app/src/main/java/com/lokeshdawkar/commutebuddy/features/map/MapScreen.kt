package com.lokeshdawkar.commutebuddy.features.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Location & SMS permissions
    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)

    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        // Request SMS permission
        if (!smsPermissionState.status.isGranted) {
            smsPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map & Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val currentLocation: GeoPoint? = locationOverlayRef?.myLocation
                    if (currentLocation != null) {
                        if (smsPermissionState.status.isGranted) {
                            sendSMSToContactsRealtimeDB(
                                context,
                                userId,
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                        } else {
                            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
                            Log.e("SMS", "Permission not granted")
                        }
                    } else {
                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                        Log.d("MAP", "Location not available")
                    }
                }
            ) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Send Location")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(28.6139, 77.2090)) // Default location

                            if (fineLocationPermission.status.isGranted) {
                                val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                                overlay.enableMyLocation()
                                overlay.enableFollowLocation()
                                overlays.add(overlay)
                                locationOverlayRef = overlay
                            }
                        }
                    },
                    update = { mapView ->
                        if (!fineLocationPermission.status.isGranted) {
                            fineLocationPermission.launchPermissionRequest()
                        }
                    }
                )
            }
        }
    )
}

 fun sendSMSToContactsRealtimeDB(context: Context, userId: String, lat: Double, lon: Double) {
    val message = "Current location: https://maps.google.com/?q=$lat,$lon"
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")

    dbRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            Toast.makeText(context, "No contacts found", Toast.LENGTH_SHORT).show()
            Log.d("SMS", "No contacts found")
            return@addOnSuccessListener
        }

        val smsManager = SmsManager.getDefault()
        var sentCount = 0
        var failCount = 0

        for (child in snapshot.children) {
            val number = child.key
            if (!number.isNullOrBlank()) {
                try {
                    smsManager.sendTextMessage(number, null, message, null, null)
                    sentCount++
                    Log.d("SMS", "SMS sent to $number")
                } catch (e: Exception) {
                    failCount++
                    Log.e("SMS", "Failed to send SMS to $number", e)
                }
            }
        }

        Toast.makeText(context, "SMS sent: $sentCount, Failed: $failCount", Toast.LENGTH_LONG).show()
        Log.d("SMS", "SMS sent: $sentCount, Failed: $failCount")
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
        Log.e("SMS", "Failed to fetch contacts", e)
    }
}
