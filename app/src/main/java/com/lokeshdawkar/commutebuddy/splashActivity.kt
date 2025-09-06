package com.lokeshdawkar.commutebuddy


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lokeshdawkar.commutebuddy.ui.theme.CommuteBuddyTheme
import kotlinx.coroutines.*

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch next activity after delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // 2 seconds
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        setContent {
            CommuteBuddyTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(320.dp)
                    )
                }
            }
        }
    }
}
