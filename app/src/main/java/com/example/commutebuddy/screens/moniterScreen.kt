package com.example.commutebuddy.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

data class UserEvent(
    val type: String = "",
    val timestamp: Long = 0L,
    val message: String = "",
    val location: String? = null
)

@Composable
fun MonitorDashboardScreen(
    monitoredUserId: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val events = remember { mutableStateListOf<UserEvent>() }
    val context = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$monitoredUserId/events")

    LaunchedEffect(monitoredUserId) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                events.clear()
                snapshot.children.forEach { eventSnap ->
                    val type = eventSnap.child("type").getValue(String::class.java) ?: ""
                    val timestamp = eventSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                    val message = eventSnap.child("message").getValue(String::class.java) ?: ""
                    val location = eventSnap.child("location").getValue(String::class.java)
                    events.add(UserEvent(type, timestamp, message, location))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = onLogout) { Text("Logout") }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Monitoring User: $monitoredUserId",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(events.reversed()) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Type: ${event.type}", fontWeight = FontWeight.Bold)
                        Text("Message: ${event.message}")
                        Text("Time: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm:ss")
                            .format(java.util.Date(event.timestamp))}")
                        event.location?.let { loc ->
                            Text(
                                "Location: $loc",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loc))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
