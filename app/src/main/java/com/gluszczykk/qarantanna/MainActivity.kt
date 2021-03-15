package com.gluszczykk.qarantanna

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_PROXIMITY
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpProximitySensor()
    }

    private fun setUpProximitySensor() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(TYPE_PROXIMITY)
        sensorManager.registerListener(
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    findViewById<TextView>(R.id.sensor_output).text = "${event.sensor.name} ${event.values[0]} cm"
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                }

            }, proximitySensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }
}