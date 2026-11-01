package com.sfx.viperglass.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class MasterTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }
    override fun onClick() {
        super.onClick()
        val i = Intent(this, ViperGlassService::class.java).apply { action = ViperGlassService.ACTION_TOGGLE }
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
    }
}

class PresetTileService : TileService() {
    override fun onClick() {
        super.onClick()
        // Cycle preset — kirim broadcast
        sendBroadcast(Intent("com.sfx.viperglass.CYCLE_PRESET"))
    }
}
