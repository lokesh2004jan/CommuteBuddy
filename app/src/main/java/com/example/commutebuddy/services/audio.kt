//package com.example.commutebuddy.services
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Intent
//import android.media.MediaRecorder
//import android.os.Build
//import android.os.Handler
//import android.os.IBinder
//import android.util.Base64
//import android.util.Log
//import android.widget.Toast
//import androidx.core.app.NotificationCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.io.FileInputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//class AudioRecordService : Service() {
//
//    private var mediaRecorder: MediaRecorder? = null
//    private var outputFile: String = ""
//    private val CHANNEL_ID = "audio_record_channel"
//
//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannel()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        startForeground(1, buildNotification("Recording audio..."))
//        startRecording()
//        return START_NOT_STICKY
//    }
//
//    private fun startRecording() {
//        try {
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val fileName = "AUDIO_$timeStamp.3gp"
//            val storageDir = getExternalFilesDir(null)
//            val file = File(storageDir, fileName)
//            outputFile = file.absolutePath
//
//            mediaRecorder = MediaRecorder().apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setOutputFile(outputFile)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                prepare()
//                start()
//            }
//
//            Log.d("AudioRecordService", "Recording started")
//            Toast.makeText(this, "Audio recording started", Toast.LENGTH_SHORT).show()
//
//            // Stop recording after 1 minute
//            Handler().postDelayed({
//                stopRecording()
//            }, 10_000)
//
//        } catch (e: Exception) {
//            Log.e("AudioRecordService", "Recording failed: ${e.message}")
//            Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun stopRecording() {
//        try {
//            mediaRecorder?.apply {
//                stop()
//                release()
//            }
//            mediaRecorder = null
//            Log.d("AudioRecordService", "Recording stopped")
//            Toast.makeText(this, "Audio recording stopped", Toast.LENGTH_SHORT).show()
//            storeInRealtimeDb()
//        } catch (e: Exception) {
//            Log.e("AudioRecordService", "Stop failed: ${e.message}")
//            Toast.makeText(this, "Stop failed: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//        stopForeground(true)
//        stopSelf()
//    }
//
//    private fun storeInRealtimeDb() {
//        try {
//            val file = File(outputFile)
//            val inputStream = FileInputStream(file)
//            val byteBuffer = ByteArrayOutputStream()
//            val buffer = ByteArray(1024)
//            var bytesRead: Int
//            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                byteBuffer.write(buffer, 0, bytesRead)
//            }
//            val audioBytes = byteBuffer.toByteArray()
//            val base64Audio = Base64.encodeToString(audioBytes, Base64.DEFAULT)
//
//            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//            val dbRef = FirebaseDatabase.getInstance()
//                .getReference("users/$userId/audio_records")
//
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            dbRef.child(timeStamp).setValue(base64Audio)
//                .addOnSuccessListener { Log.d("AudioRecordService", "Audio stored in Realtime DB") }
//                .addOnFailureListener { e -> Log.e("AudioRecordService", "DB store failed: ${e.message}") }
//
//        } catch (e: Exception) {
//            Log.e("AudioRecordService", "Failed to store audio: ${e.message}")
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Audio Recording",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Recording audio in background"
//            }
//            val manager = getSystemService(NotificationManager::class.java)
//            manager?.createNotificationChannel(channel)
//        }
//    }
//
//    private fun buildNotification(contentText: String): Notification {
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("Commute Buddy")
//            .setContentText(contentText)
//            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
//            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimal priority
//            .setCategory(NotificationCompat.CATEGORY_SERVICE)
//            .build()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//}
