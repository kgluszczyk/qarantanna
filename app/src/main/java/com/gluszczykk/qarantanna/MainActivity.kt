package com.gluszczykk.qarantanna

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_PROXIMITY
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {

        const val ChannelId = "1"
        const val NotificationId = 1
        const val CameraKey = "CameraKey"
        const val AlarmRequestCode = 123
    }

    private var image: Bitmap? = null

    private val startCameraForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            image = result.data?.extras?.get("data") as Bitmap
        }
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
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        setUpProximitySensor(sensorManager)
        setUpAccelerometerSensor(sensorManager)
        createNotificationChannel(notificationManager)
        observeData(notificationManager)

        setUpLogo()
        scheduleAlarm()
    }

    private fun scheduleAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, VideoActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, AlarmRequestCode, intent, 0)
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10_000, pendingIntent)
    }

    private fun setUpLogo() {
        Glide
            .with(this)
            .load("https://png.pngtree.com/element_pic/00/16/07/115783931601b5c.jpg")
            .centerInside()
            .placeholder(R.drawable.ic_launcher_background)
            .into(findViewById(R.id.logo))
    }

    override fun onDestroy() {
        super.onDestroy()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationId)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(CameraKey, false)) {
            startCameraForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }
    }

    private fun createNotification(notificationManager: NotificationManager, notificationText: String) {
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, 1, contentIntent, 0)

        val musicIntent = Intent(this, MusicService::class.java)
        val musicPendingIntent = PendingIntent.getService(this, 2, musicIntent, 0)

        val cameraIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(CameraKey, true)
        }
        val cameraPendingIntent = PendingIntent.getActivity(this, 3, cameraIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_baseline_data_usage_24)
            .setContentTitle("Sensory")
            .setContentText(notificationText)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_baseline_data_usage_24, "Music", musicPendingIntent)
            .addAction(R.drawable.ic_baseline_data_usage_24, "Camera", cameraPendingIntent)

        if (image != null) {
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle())
        }
        notificationManager.notify(NotificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                ChannelId,
                "Sensory",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun observeData(notificationManager: NotificationManager) {
        lifecycleScope.launch {
            combine(
                accelerometerData.consumeAsFlow().distinctUntilChangedBy { it.value },
                proximityData.consumeAsFlow().distinctUntilChangedBy { it.value }
            ) { accelerometerEvent, proximityEvent -> accelerometerEvent + proximityEvent }
                .collect { sensorOutput ->
                    sensorOutput.map { sensorOutput -> sensorOutput.toDisplayValue() }
                        .reduce { acc, sensorOutput ->
                            "$acc\n$sensorOutput"
                        }.also { label ->
                            createNotification(notificationManager, label)
                            findViewById<TextView>(R.id.sensor_output).text = label
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
