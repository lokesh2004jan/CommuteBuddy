//package com.example.commutebuddy.features.auth
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.firestore.ktx.firestore
//@Composable
//fun AuthScreen(onAuthenticated: () -> Unit) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var error by remember { mutableStateOf<String?>(null) }
//    val auth = remember { FirebaseAuth.getInstance() }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp)
//    ) {
//        Text("Sign in or create an account")
//
//        Spacer(Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//            singleLine = true
//        )
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//            singleLine = true
//        )
//
//        error?.let { Text(it, color = Color.Red) }
//
//        Spacer(Modifier.height(16.dp))
//
//        // ✅ Sign In Button
//        Button(
//            onClick = {
//                if (email.isBlank() || password.isBlank()) {
//                    error = "Email and password cannot be empty"
//                    return@Button
//                }
//                error = null
//                auth.signInWithEmailAndPassword(email.trim(), password)
//                    .addOnSuccessListener { onAuthenticated() }
//                    .addOnFailureListener { error = it.localizedMessage }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Sign In")
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // ✅ Sign Up Button (Create Account)
//        Button(
//            onClick = {
//                if (email.isBlank() || password.isBlank()) {
//                    error = "Email and password cannot be empty"
//                    return@Button
//                }
//                if (password.length < 6) {
//                    error = "Password must be at least 6 characters"
//                    return@Button
//                }
//
//                error = null
//                auth.createUserWithEmailAndPassword(email.trim(), password)
//                    .addOnSuccessListener { result ->
//                        val uid = result.user?.uid ?: return@addOnSuccessListener
//                        val profile = mapOf(
//                            "uid" to uid,
//                            "email" to email.trim(),
//                            "createdAt" to System.currentTimeMillis()
//                        )
//                        Firebase.firestore.collection("users").document(uid).set(profile)
//                            .addOnCompleteListener { onAuthenticated() }
//                    }
//                    .addOnFailureListener { error = it.localizedMessage }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // green
//        ) {
//            Text("Create Account")
//        }
//    }
//}
