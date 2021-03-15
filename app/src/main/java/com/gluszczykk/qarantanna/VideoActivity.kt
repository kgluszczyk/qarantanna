package com.gluszczykk.qarantanna

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        findViewById<VideoView>(R.id.video).run {
            setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.video}"))
            start()
        }
    }
}