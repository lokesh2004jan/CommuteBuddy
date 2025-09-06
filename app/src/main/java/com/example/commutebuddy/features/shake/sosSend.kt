package com.example.commutebuddy.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.commutebuddy.features.shake.ShakeDetector
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SOSService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private var volumeDownPressedTime = 0L
    private val longPressDuration = 5000L

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        initShakeDetector()
    }

    private fun startForegroundService() {
        val channelId = "SOS_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOS Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOS Service Active")
            .setContentText("Shake or press volume 5 sec to send SOS")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()

        startForeground(1, notification)
    }

    private fun initShakeDetector() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector {
            Log.d("SOSService", "Shake detected → sending SOS")
            sendSos()
        }
        sensorManager.registerListener(shakeDetector, sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
    }

    fun onVolumeKeyDown() {
        volumeDownPressedTime = System.currentTimeMillis()
    }

    fun onVolumeKeyUp() {
        val pressedTime = System.currentTimeMillis() - volumeDownPressedTime
        if (pressedTime >= longPressDuration) {
            Log.d("SOSService", "Volume long press → sending SOS")
            sendSos()
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendSos() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val contactsRef = FirebaseDatabase.getInstance().getReference("users/$uid/contacts")

        // Get last location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val lat = location?.latitude ?: 0.0
            val lon = location?.longitude ?: 0.0
            val message = "SOS! Help needed! Location: https://maps.google.com/?q=$lat,$lon"

            // Send SMS to all contacts
            contactsRef.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { contact ->
                    val phone = contact.key
                    if (!phone.isNullOrEmpty()) {
                        try {
                            SmsManager.getDefault().sendTextMessage(phone, null, message, null, null)
                            Log.d("SOSService", "SMS sent to $phone")
                        } catch (e: Exception) {
                            Log.e("SOSService", "SMS failed: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector)
    }
}
