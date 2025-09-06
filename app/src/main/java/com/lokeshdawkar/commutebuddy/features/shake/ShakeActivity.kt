//package com.lokeshdawkar.commutebuddy.features.shake
//import android.hardware.Sensor
//import android.hardware.SensorManager
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//
//class ShakeTestActivity : AppCompatActivity() {
//
//    private lateinit var sensorManager: SensorManager
//    private lateinit var shakeDetector: ShakeDetector
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//
//        shakeDetector = ShakeDetector {
//            // This lambda will be called when shake is detected
//            Toast.makeText(this, "Shake detected!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
//            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(shakeDetector)
//    }
//}
