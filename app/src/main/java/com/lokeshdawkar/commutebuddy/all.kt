package com.lokeshdawkar.commutebuddy

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.lokeshdawkar.commutebuddy.features.shake.ShakeDetector
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CommuteBuddyService : Service() {

    private val CHANNEL_ID = "CommuteBuddyChannel"
    private val ALERT_CHANNEL_ID = "AccidentAlertChannel"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val exitedGeofences = mutableSetOf<String>()

    // 🔹 Shake detection
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector

    // 🔹 Current location storage
    private var currentLocation: android.location.Location? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(
            1,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Commute Buddy Active")
                .setContentText("Monitoring location, geofences & shake")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()

        // Start Shake detection
        startShakeDetection()
    }

    private fun startShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        shakeDetector = ShakeDetector {
            Log.d("CommuteBuddyService", "Shake detected!")

            val locationText = currentLocation?.let {
                "https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "Location not available"

            // Show toast alert
            Toast.makeText(
                this,
                "⚠️ Possible accident detected!\n$locationText",
                Toast.LENGTH_LONG
            ).show()

            // 🔹 Show notification
            showAccidentNotification(locationText)

            // 🔹 Send SOS
            sendShakeSms()
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun sendShakeSms() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val contactsRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")

        contactsRef.get().addOnSuccessListener { contactsSnap ->
            if (!contactsSnap.exists()) {
                Log.w("CommuteBuddyService", "No contacts found in DB")
                return@addOnSuccessListener
            }

            val smsManager = SmsManager.getDefault()

            // Build message with current location
            val locationText = currentLocation?.let {
                "https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "Location not available"

            val message = "🚨 SOS Alert: Possible accident detected!\nLocation: $locationText"

            contactsSnap.children.forEach { contact ->
                // 🔹 Support both cases: phone as key OR phone inside object
                val phone = contact.key ?: contact.child("phone").getValue(String::class.java)

                if (!phone.isNullOrEmpty()) {
                    try {
                        smsManager.sendTextMessage(phone, null, message, null, null)
                        Log.d("CommuteBuddyService", "✅ Shake SOS sent to $phone")
                    } catch (e: Exception) {
                        Log.e("CommuteBuddyService", "❌ Failed to send SOS to $phone: ${e.message}")
                    }
                } else {
                    Log.w("CommuteBuddyService", "Skipping invalid contact: $contact")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("CommuteBuddyService", "Failed to fetch contacts: ${e.message}")
        }
    }


    // 🔹 Local notification for accident alert
    private fun showAccidentNotification(locationText: String) {
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("🚨 Accident Detected")
            .setContentText("SOS sent to contacts! $locationText")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    // 🔹 Location updates to track current position
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000L
            fastestInterval = 2000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    currentLocation = location // 🔹 Save last known location
                    checkGeofences(location.latitude, location.longitude)
                }
            },
            mainLooper
        )
    }

    private fun checkGeofences(userLat: Double, userLon: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val geofencesRef = FirebaseDatabase.getInstance().getReference("users/$userId/geofences")
        val contactsRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")

        geofencesRef.get().addOnSuccessListener { geoSnapshot ->
            geoSnapshot.children.forEach { geo ->
                val id = geo.key ?: return@forEach
                val lat = geo.child("lat").getValue(Double::class.java) ?: return@forEach
                val lon = geo.child("lon").getValue(Double::class.java) ?: return@forEach
                val radius = geo.child("radius").getValue(Double::class.java) ?: return@forEach
                val message = geo.child("message").getValue(String::class.java) ?: return@forEach

                val distance = distanceInMeters(userLat, userLon, lat, lon)
                if (distance > radius && !exitedGeofences.contains(id)) {
                    exitedGeofences.add(id)

                    contactsRef.get().addOnSuccessListener { contactsSnap ->
                        val smsMessage = "$message Location: https://maps.google.com/?q=$userLat,$userLon"
                        contactsSnap.children.forEach { contact ->
                            val phone = contact.key
                            if (!phone.isNullOrEmpty()) sendSms(phone, smsMessage)
                        }
                    }
                } else if (distance <= radius && exitedGeofences.contains(id)) {
                    exitedGeofences.remove(id)
                }
            }
        }
    }

    private fun sendSms(number: String, message: String) {
        try {
            SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
        } catch (_: Exception) {}
    }

    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radiusEarth = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radiusEarth * c
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Commute Buddy Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Accident Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
        sensorManager.unregisterListener(shakeDetector) // 🔹 stop shake listener
    }
}
