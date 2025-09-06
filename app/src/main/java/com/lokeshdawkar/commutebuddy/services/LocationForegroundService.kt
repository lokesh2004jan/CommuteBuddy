//package com.lokeshdawkar.commutebuddy.services
//
//import android.Manifest
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Intent
//import android.location.Location
//import android.os.Build
//import android.os.IBinder
//import androidx.annotation.RequiresPermission
//import androidx.core.app.NotificationCompat
//import com.lokeshdawkar.commutebuddy.R
//import com.google.android.gms.location.*
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//
//class LocationForegroundService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    override fun onCreate() {
//        super.onCreate()
//        createChannel()
//
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(getString(R.string.app_name))
//            .setContentText("Location tracking active")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .build()
//        startForeground(NOTIFICATION_ID, notification)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // Setup location callback
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location: Location in result.locations) {
//                    sendLocationToFirebase(location)
//                }
//            }
//        }
//
//        requestLocationUpdates()
//    }
//
//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun requestLocationUpdates() {
//        val request = LocationRequest.Builder(10_000) // every 10 seconds
//            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//            .build()
//
//        // Permission check must be handled before starting service
//        fusedLocationClient.requestLocationUpdates(
//            request,
//            locationCallback,
//            mainLooper
//        )
//    }
//
//    private fun sendLocationToFirebase(location: Location) {
//        val user = FirebaseAuth.getInstance().currentUser ?: return
//        val dbRef = FirebaseDatabase.getInstance().getReference("locations").child(user.uid)
//
//        val data = mapOf(
//            "lat" to location.latitude,
//            "lng" to location.longitude,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        dbRef.setValue(data)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        // Service restarts if killed
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    private fun createChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Location",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val nm = getSystemService(NotificationManager::class.java)
//            nm.createNotificationChannel(channel)
//        }
//    }
//
//    companion object {
//        private const val CHANNEL_ID = "location_channel"
//        private const val NOTIFICATION_ID = 1001
//    }
//}
