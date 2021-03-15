package com.gluszczykk.qarantanna

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_PROXIMITY
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {

        const val ChannelId = "1"
    }

    val accelerometerData = Channel<SensorOutput>(Channel.UNLIMITED)
    val proximityData = Channel<SensorOutput>(Channel.UNLIMITED)

    data class SensorOutput(val name: String, val value: String, val timestamp: String)

    private operator fun SensorOutput.plus(proximityEvent: SensorOutput) = listOf(this, proximityEvent)

    fun SensorOutput.toDisplayValue() = "${name}, ${value}, ${timestamp}"

    fun SensorEvent.toLabel() = when (sensor.type) {
        TYPE_PROXIMITY -> "${sensor.name} ${values[0]} cm"
        TYPE_ACCELEROMETER -> "${sensor.name} x:${values[0]} y:${values[1]},z ${values[2]}"
        else -> ""
    }

    fun SensorEvent.toSensorOutput() = SensorOutput(sensor.name, toLabel(), "$timestamp")

    private val sensorEventListener = { dataStream: Channel<SensorOutput> ->
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                dataStream.offer(event.toSensorOutput())
            }

            override fun onAccuracyChanged(sensorOutput: Sensor, accuracy: Int) {
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        setUpProximitySensor(sensorManager)
        setUpAccelerometerSensor(sensorManager)
        observeData()
        createNotification()
    }

    private fun createNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                ChannelId,
                "Sensory",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, 1, contentIntent, 0)
        val notificationBuilder = NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_baseline_data_usage_24)
            .setContentTitle("Sensory")
            .setContentIntent(contentPendingIntent)
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun observeData() {
        CoroutineScope(Dispatchers.Main).launch {
            combine(
                accelerometerData.consumeAsFlow().distinctUntilChangedBy { it.value },
                proximityData.consumeAsFlow().distinctUntilChangedBy { it.value }
            ) { accelerometerEvent, proximityEvent -> accelerometerEvent + proximityEvent }
                .collect { sensorOutput ->
                    sensorOutput.map { sensorOutput -> sensorOutput.toDisplayValue() }
                        .reduce { acc, sensorOutput ->
                            "$acc\n$sensorOutput"
                        }.also {
                            findViewById<TextView>(R.id.sensor_output).text = it
                        }
                }
        }
    }

    private fun setUpProximitySensor(sensorManager: SensorManager) {
        val proximitySensor = sensorManager.getDefaultSensor(TYPE_PROXIMITY)
        sensorManager.registerListener(
            sensorEventListener(proximityData), proximitySensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun setUpAccelerometerSensor(sensorManager: SensorManager) {
        val proximitySensor = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            sensorEventListener(accelerometerData), proximitySensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

}
