package com.lokeshdawkar.commutebuddy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lokeshdawkar.commutebuddy.features.OptionsScreen
import com.lokeshdawkar.commutebuddy.features.map.MapScreen
import com.lokeshdawkar.commutebuddy.features.auth.LoginScreen
import com.lokeshdawkar.commutebuddy.features.auth.SignupScreen
import com.lokeshdawkar.commutebuddy.screens.*
import com.lokeshdawkar.commutebuddy.ui.theme.CommuteBuddyTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val permissionsGranted = mutableStateOf(false)
    private val missingPermissions = mutableStateOf(listOf<String>())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val stillMissing = permissions.filter { !it.value }.map { it.key ?: "" }
            missingPermissions.value = stillMissing
            permissionsGranted.value = stillMissing.isEmpty()
            if (permissionsGranted.value) startCommuteBuddyService()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestAllPermissions()

        setContent {
            CommuteBuddyTheme {
                val auth = FirebaseAuth.getInstance()
                var currentScreen by remember { mutableStateOf("loading") }
                val granted by remember { permissionsGranted }
                val missing by remember { missingPermissions }

                LaunchedEffect(granted, missing) {
                    currentScreen = when {
                        !granted -> "permissions"
                        auth.currentUser != null -> "options"
                        else -> "login"
                    }
                }

                when {
                    currentScreen == "loading" -> Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) { Text("Initializing...", color = Color.White) }

                    currentScreen == "permissions" -> Column(
                        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("The app needs the following permissions:", color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        missing.forEach { perm -> Text("- $perm", color = Color.Red) }
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { requestPermissionLauncher.launch(missing.toTypedArray()) }) {
                            Text("Grant Permissions")
                        }
                    }

                    currentScreen == "login" -> LoginScreen(
                        onAuthenticated = { currentScreen = "options" },
                        onNavigateToSignup = { currentScreen = "signup" },
                        onMonitorLogin = { currentScreen = "monitorLogin" } // Add monitor login button
                    )

                    currentScreen == "signup" -> SignupScreen(
                        onAuthenticated = { currentScreen = "options" },
                        onBack = { currentScreen = "login" }
                    )





                    currentScreen == "options" -> OptionsScreen(
                        onMapClick = { currentScreen = "map" },
                        onContactsClick = { currentScreen = "contacts" },
                        onSosClick = { currentScreen = "sos" },
                        onGestureClick = { currentScreen = "gesture" },
                        onAboutClick = { currentScreen = "about" },
                        onExitClick = { finish() },
                        onBack = { currentScreen = "options" }
                    )

                    currentScreen == "map" -> MapScreen(onBack = { currentScreen = "options" })
                    currentScreen == "contacts" -> ContactsScreen(onBack = { currentScreen = "options" })
                    currentScreen == "sos" -> GeofenceSetupScreen(onBack = { currentScreen = "options" })
                    currentScreen == "gesture" -> GestureScreen(onBack = { currentScreen = "options" })
                    currentScreen == "about" -> AboutUsScreen(
                        onBack = { currentScreen = "options" },
                        onLogout = { currentScreen = "login" }
                    )
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            Manifest.permission.SEND_SMS,
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        missingPermissions.value = missing
        if (missing.isNotEmpty()) requestPermissionLauncher.launch(missing.toTypedArray())
        else { permissionsGranted.value = true; startCommuteBuddyService() }
    }

    private fun startCommuteBuddyService() {
        val serviceIntent = Intent(this, CommuteBuddyService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
