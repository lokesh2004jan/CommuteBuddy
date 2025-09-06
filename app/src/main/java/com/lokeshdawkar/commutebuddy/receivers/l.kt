//package com.lokeshdawkar.commutebuddy.receivers
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import com.lokeshdawkar.commutebuddy.services.GeofenceTransitionsService
//import com.google.android.gms.location.Geofence
//import com.google.android.gms.location.GeofencingEvent
//
//class GeofenceBroadcastReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
//
//        if (geofencingEvent.hasError()) {
//            Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
//            return
//        }
//
//        when (geofencingEvent.geofenceTransition) {
//            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                Log.d("GeofenceReceiver", "Entered geofence")
//            }
//            Geofence.GEOFENCE_TRANSITION_EXIT -> {
//                Log.d("GeofenceReceiver", "Exited geofence")
//            }
//            Geofence.GEOFENCE_TRANSITION_DWELL -> {
//                Log.d("GeofenceReceiver", "Dwelling in geofence")
//            }
//        }
//
//        // Forward to service for heavy work
//        val serviceIntent = Intent(context, GeofenceTransitionsService::class.java).apply {
//            putExtra("transition", geofencingEvent.geofenceTransition)
//        }
//        context.startService(serviceIntent)
//    }
//}
