package com.example.commutebuddy.features

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commutebuddy.R

@Composable
fun OptionsScreen(
    onMapClick: () -> Unit,
    onContactsClick: () -> Unit,
    onSosClick: () -> Unit,
    onGestureClick: () -> Unit,
    onAboutClick: () -> Unit,
    onExitClick: () -> Unit,
    onBack: () -> Unit
) {
    // Handle mobile back button
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark background
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose an Option",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
            color = Color(0xFFBB86FC),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OptionCard("Map", R.drawable.ic_map, Modifier.weight(1f), onMapClick)
                OptionCard("Contacts", R.drawable.ic_contacts, Modifier.weight(1f), onContactsClick)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OptionCard("SOS", R.drawable.ic_sos, Modifier.weight(1f), onSosClick)
                OptionCard("Gestures", R.drawable.ic_gesture, Modifier.weight(1f), onGestureClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OptionCard("About Us", R.drawable.ic_info, Modifier.weight(1f), onAboutClick)
                OptionCard("Exit", R.drawable.ic_exit, Modifier.weight(1f), onExitClick)
            }
        }
    }
}

@Composable
fun OptionCard(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color(0xFFBB86FC),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
