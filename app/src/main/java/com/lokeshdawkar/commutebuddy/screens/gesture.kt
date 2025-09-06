package com.lokeshdawkar.commutebuddy.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lokeshdawkar.commutebuddy.features.shake.ShakeDetector

@Composable
fun GestureScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    BackHandler { onBack() }
    var shakeStatus by remember { mutableStateOf("Shake your phone to send SOS") }
    var volumeStatus by remember { mutableStateOf("Hold volume down for 5 seconds to send SOS") }

    val gradientColors = listOf(Color(0xFFBB86FC), Color(0xFF3700B3))

    // Shake detection
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val shakeDetector = remember {
        ShakeDetector {
            Log.d("SOS", "Shake detected → sending SOS")
            shakeStatus = "Shake detected! SOS sent."
            sendSOS(context)
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(
            shakeDetector,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose { sensorManager.unregisterListener(shakeDetector) }
    }

    // Volume key long press detection
    var volumeDownPressedTime by remember { mutableStateOf(0L) }
    val longPressDuration = 5000L

    // Back handler
    BackHandler { onBack() }

    activity.window.callback = object : android.view.Window.Callback by activity.window.callback {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    volumeDownPressedTime = SystemClock.elapsedRealtime()
                }
                KeyEvent.ACTION_UP -> if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    val duration = SystemClock.elapsedRealtime() - volumeDownPressedTime
                    if (duration >= longPressDuration) {
                        Log.d("SOS", "Volume long press detected → sending SOS")
                        volumeStatus = "Volume long press detected! SOS sent."
                        sendSOS(context)
                    }
                }
            }
            return activity.window.superDispatchKeyEvent(event)
        }
    }

    val shakeColor by animateColorAsState(
        if (shakeStatus.contains("SOS sent")) Color(0xFFFFCDD2) else Color(0xFF1E1E1E)
    )
    val volumeColor by animateColorAsState(
        if (volumeStatus.contains("SOS sent")) Color(0xFFBBDEFB) else Color(0xFF1E1E1E)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()

            .background(Color(0xFF121212)).padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Gesture SOS",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = Color.White,
            modifier = Modifier.padding(all = 24.dp)
        )

        // Shake Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = shakeColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Shake Icon",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = shakeStatus,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Volume Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = volumeColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Volume Icon",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = volumeStatus,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons Row with Gradient
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GradientButton(
                text = "Back",
                modifier = Modifier.weight(1f),
                gradientColors = gradientColors,
                onClick = onBack
            )


        }
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(12.dp)
                )
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

fun sendSOS(context: Context) {
    Log.d("SOS", "Sending SOS! Fetch contacts, get location, send SMS...")
}
