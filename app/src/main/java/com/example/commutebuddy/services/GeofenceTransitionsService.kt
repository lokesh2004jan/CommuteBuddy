package com.example.commutebuddy.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.os.Handler
import android.os.Looper
import android.location.Location
import android.location.LocationManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.telephony.SmsManager

class ManualGeofenceService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L // 5 seconds
    private var geofenceExited = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCheckingLocation()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startCheckingLocation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkLocationAndGeofences()
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun checkLocationAndGeofences() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return

        val userLat = location.latitude
        val userLon = location.longitude
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val geofencesRef = FirebaseDatabase.getInstance().getReference("users/$userId/geofences")
        val contactsRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")

        geofencesRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { geo ->
                val lat = geo.child("lat").getValue(Double::class.java) ?: return@forEach
                val lon = geo.child("lon").getValue(Double::class.java) ?: return@forEach
                val radius = geo.child("radius").getValue(Double::class.java) ?: return@forEach
                val message = geo.child("message").getValue(String::class.java) ?: "Exited geofence!"

                val distance = distanceInMeters(userLat, userLon, lat, lon)
                if (distance > radius && !geofenceExited) {
                    geofenceExited = true
                    Log.d("GeofenceService", "User exited geofence → send SMS")
                    sendSmsToContacts(contactsRef, message, userLat, userLon)
                } else if (distance <= radius) {
                    geofenceExited = false
                }
            }
        }
    }

    private fun sendSmsToContacts(contactsRef: com.google.firebase.database.DatabaseReference, message: String, lat: Double, lon: Double) {
        val finalMessage = "$message\nLocation: https://maps.google.com/?q=$lat,$lon"
        contactsRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { contact ->
                val phone = contact.key ?: return@forEach
                try {
                    SmsManager.getDefault().sendTextMessage(phone, null, finalMessage, null, null)
                    Log.d("GeofenceService", "SMS sent to $phone")
                } catch (e: Exception) {
                    Log.e("GeofenceService", "Failed to send SMS: ${e.message}")
                }
            }
        }
    }

    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radius * c
    }

    private fun Double.pow(exp: Double): Double = Math.pow(this, exp)
}
