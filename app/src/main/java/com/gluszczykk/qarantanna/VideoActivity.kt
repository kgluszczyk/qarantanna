package com.gluszczykk.qarantanna

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        findViewById<VideoView>(R.id.video).run {
            val mediaController = MediaController(this@VideoActivity).apply {
                setAnchorView(this@run)
            }
            setMediaController(mediaController)
            setVideoURI(Uri.parse("https://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4"))
            requestFocus()
            start()
        }
    }
}