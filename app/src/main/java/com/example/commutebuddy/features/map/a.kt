package com.example.commutebuddy.features.map


import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GeofenceActivity : AppCompatActivity() {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var pendingIntent: PendingIntent
    private lateinit var sosMessage: String
    private var geofenceId = "custom_geofence"

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )

        val lat = intent.getDoubleExtra("latitude", 0.0)
        val lon = intent.getDoubleExtra("longitude", 0.0)
        val radius = intent.getFloatExtra("radius", 100f)
        sosMessage = intent.getStringExtra("sosMessage") ?: "SOS! I need help!"

        geofencingClient = LocationServices.getGeofencingClient(this)

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(lat, lon, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geofence added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add geofence", Toast.LENGTH_SHORT).show()
            }
    }

    // BroadcastReceiver to handle geofence transitions
    class GeofenceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val transitionType = intent?.getIntExtra("transition", -1)
            if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                context?.let { sendSosToContacts(it) }
            }
        }

        private fun sendSosToContacts(context: Context) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")

            dbRef.get().addOnSuccessListener { snapshot ->
                val smsManager = SmsManager.getDefault()
                val message = "SOS! I exited geofence. My location: https://maps.google.com"

                snapshot.children.forEach { child ->
                    val number = child.key
                    if (!number.isNullOrBlank()) {
                        smsManager.sendTextMessage(number, null, message, null, null)
                    }
                }
                Toast.makeText(context, "SOS sent to all contacts", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to send SOS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        geofencingClient.removeGeofences(listOf(geofenceId))
    }
}
