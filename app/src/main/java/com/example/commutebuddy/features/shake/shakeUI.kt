//package com.example.commutebuddy.features.shake
//
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorManager
//import android.telephony.SmsManager
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.runtime.*
//import androidx.compose.ui.platform.LocalContext
//import com.google.firebase.database.FirebaseDatabase
//import kotlin.math.sqrt
//@Composable
//fun ShakeTestComposable(userId: String) {
//    val context = LocalContext.current
//    var shakeDetected by remember { mutableStateOf(false) }
//    var lastShakeTime by remember { mutableStateOf(0L) }
//
//    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
//
//    val shakeDetector = remember {
//        ShakeDetector {
//            val now = System.currentTimeMillis()
//            if (now - lastShakeTime > 5000) { // 5-second debounce
//                shakeDetected = true
//                lastShakeTime = now
//            }
//        }
//    }
//
//    DisposableEffect(Unit) {
//        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//        onDispose {
//            sensorManager.unregisterListener(shakeDetector)
//        }
//    }
//
//    LaunchedEffect(shakeDetected) {
//        if (shakeDetected) {
//            Toast.makeText(context, "Shake detected! Sending SMS...", Toast.LENGTH_SHORT).show()
//
//            val geofenceRef = FirebaseDatabase.getInstance().getReference("users/$userId/geofences")
//            geofenceRef.get().addOnSuccessListener { snapshot ->
//                if (!snapshot.exists()) {
//                    Toast.makeText(context, "No geofences found", Toast.LENGTH_SHORT).show()
//                    return@addOnSuccessListener
//                }
//
//                val contactsRef = FirebaseDatabase.getInstance().getReference("users/$userId/contacts")
//                contactsRef.get().addOnSuccessListener { contactsSnap ->
//                    if (!contactsSnap.exists()) {
//                        Toast.makeText(context, "No contacts found", Toast.LENGTH_SHORT).show()
//                        return@addOnSuccessListener
//                    }
//
//                    val phoneNumbers = contactsSnap.children.mapNotNull { it.key }
//                    val smsManager = SmsManager.getDefault()
//                    var sentCount = 0
//                    var failCount = 0
//
//                    for (geo in snapshot.children) {
//                        val lat = geo.child("lat").getValue(Double::class.java)
//                        val lon = geo.child("lon").getValue(Double::class.java)
//                        val messageText = geo.child("message").getValue(String::class.java)
//
//                        if (lat != null && lon != null && !messageText.isNullOrBlank()) {
//                            val finalMessage = "$messageText\nLocation: https://maps.google.com/?q=$lat,$lon"
//                            for (number in phoneNumbers) {
//                                try {
//                                    smsManager.sendTextMessage(number, null, finalMessage, null, null)
//                                    sentCount++
//                                } catch (e: Exception) {
//                                    failCount++
//                                }
//                            }
//                        }
//                    }
//
//                    Toast.makeText(context, "SMS sent: $sentCount, Failed: $failCount", Toast.LENGTH_LONG).show()
//                }.addOnFailureListener {
//                    Toast.makeText(context, "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
//                }
//
//            }.addOnFailureListener {
//                Toast.makeText(context, "Failed to fetch geofences", Toast.LENGTH_SHORT).show()
//            }
//
//            shakeDetected = false
//        }
//    }
//}
