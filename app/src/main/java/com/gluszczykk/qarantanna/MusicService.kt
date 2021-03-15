package com.gluszczykk.qarantanna

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaPlayer().run {
            setDataSource("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_1MG.mp3")
            prepare()
            start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}