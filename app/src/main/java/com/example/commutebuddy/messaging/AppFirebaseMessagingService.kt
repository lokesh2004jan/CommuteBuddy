//package com.example.commutebuddy.services
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import android.telephony.SmsManager
//import android.util.Log
//import com.google.android.gms.location.Geofence
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.firestore.FirebaseFirestore
//
//class GeofenceTransitionsService : Service() {
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val transition = intent?.getIntExtra("transition", -1)
//
//        when (transition) {
//            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                Log.d("GeofenceService", "Entered geofence")
//            }
//            Geofence.GEOFENCE_TRANSITION_EXIT -> {
//                Log.d("GeofenceService", "Exited geofence → sending SOS")
//                sendSOSMessage()
//            }
//            Geofence.GEOFENCE_TRANSITION_DWELL -> {
//                Log.d("GeofenceService", "Dwelling in geofence")
//            }
//        }
//
//        stopSelf()
//        return START_NOT_STICKY
//    }
//
//    private fun sendSOSMessage() {
//        val user = FirebaseAuth.getInstance().currentUser ?: return
//        val uid = user.uid
//        val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/contacts")
//
//        dbRef.get().addOnSuccessListener { snapshot ->
//            snapshot.children.forEach { contact ->
//                val phone = contact.key
//                phone?.let {
//                    val message = "SOS! ${user.displayName ?: "Your contact"} has crossed the geofence!"
//                    sendSMS(phone, message)
//                }
//            }
//        }.addOnFailureListener {
//            Log.e("GeofenceService", "Failed to fetch contacts: ${it.message}")
//        }
//    }
//
//    private fun sendSMS(phoneNumber: String, message: String) {
//        try {
//            val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
//            Log.d("GeofenceService", "SMS sent to $phoneNumber")
//        } catch (e: Exception) {
//            Log.e("GeofenceService", "SMS failed: ${e.message}")
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//}
