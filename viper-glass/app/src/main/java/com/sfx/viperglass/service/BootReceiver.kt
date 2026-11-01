package com.sfx.viperglass.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            val svc = Intent(c, ViperGlassService::class.java).apply { action = ViperGlassService.ACTION_START }
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(svc) else c.startService(svc)
        }
    }
}
