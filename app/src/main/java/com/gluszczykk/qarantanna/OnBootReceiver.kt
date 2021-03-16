package com.gluszczykk.qarantanna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("BOOT", "ZBOOTOWAŁEM!")
    }


}