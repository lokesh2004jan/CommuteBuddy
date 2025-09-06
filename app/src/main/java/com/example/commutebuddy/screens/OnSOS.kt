package com.example.commutebuddy.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/** ------------------- Gradient Button ------------------- **/
val gradientColors = listOf(
    Color(0xFFBB86FC),
    Color(0xFF3700B3)
)

@Composable
fun GradientOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.linearGradient(gradientColors)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

/** ------------------- Geofence Setup Screen ------------------- **/
@Composable
fun GeofenceSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // If user not logged in
    if (userId == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            onBack()
        }
        return
    }

    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/geofences")

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var sosMessage by remember { mutableStateOf("") }

    // Hardware back button
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp)
    ) {
        // Header with back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Set Geofence & SOS",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Form container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { input -> latitude = input.filter { it.isDigit() || it == '.' || it == '-' } },
                        label = { Text("Latitude") },
                        placeholder = { Text("e.g. 19.0599") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBB86FC),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFBB86FC),
                            unfocusedLabelColor = Color.LightGray
                        )
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { input -> longitude = input.filter { it.isDigit() || it == '.' || it == '-' } },
                        label = { Text("Longitude") },
                        placeholder = { Text("e.g. 72.869203") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBB86FC),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFBB86FC),
                            unfocusedLabelColor = Color.LightGray
                        )
                    )
                }

                OutlinedTextField(
                    value = radius,
                    onValueChange = { input -> radius = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Radius (meters)") },
                    placeholder = { Text("e.g. 300") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFBB86FC),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFFBB86FC),
                        unfocusedLabelColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = sosMessage,
                    onValueChange = { sosMessage = it },
                    label = { Text("SOS Message") },
                    placeholder = { Text("Message to send when triggered") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFBB86FC),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFFBB86FC),
                        unfocusedLabelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GradientOutlinedButton(
                text = "Save Geofence",
                modifier = Modifier.weight(1f),
                onClick = {
                    if (latitude.isBlank() || longitude.isBlank() || radius.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@GradientOutlinedButton
                    }

                    try {
                        val latDouble = latitude.toDouble()
                        val lonDouble = longitude.toDouble()
                        val radDouble = radius.toDouble()

                        dbRef.child("current").setValue(
                            mapOf(
                                "lat" to latDouble,
                                "lon" to lonDouble,
                                "radius" to radDouble,
                                "message" to sosMessage
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(context, "Geofence saved!", Toast.LENGTH_SHORT).show()
                            latitude = ""
                            longitude = ""
                            radius = ""
                            sosMessage = ""
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to save geofence", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Invalid input! Enter valid numbers.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            GradientOutlinedButton(
                text = "Cancel",
                modifier = Modifier.weight(1f),
                onClick = onBack
            )
        }
    }
}
