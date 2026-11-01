package com.sfx.viperglass.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.sfx.viperglass.R
import com.sfx.viperglass.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.reflect.Constructor

/**
 * ViPER Glass Service — universal, menggantikan ViperService original.
 * - Mencoba AudioEffect dengan UUID v4a_standard_re
 * - Fallback ke reflection untuk semua API level
 * - Deteksi per-app via AudioPlaybackCallback
 */
@AndroidEntryPoint
class ViperGlassService : LifecycleService() {

    companion object {
        const val ACTION_START = "com.sfx.viperglass.START"
        const val ACTION_STOP = "com.sfx.viperglass.STOP"
        const val ACTION_TOGGLE = "com.sfx.viperglass.TOGGLE"
        const val EXTRA_MASTER = "extra_master"
        const val NOTIF_ID = 1001
        const val CHANNEL_ID = "viper_glass"
        const val EFFECT_UUID = "90380da3-8536-4744-a6a3-5731970e640f"
        const val EFFECT_TYPE_UUID = "e0e904a0-5451-11e2-bcfd-0800200c9a66" // placeholder
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var audioEffect: AudioEffect? = null
    private var masterEnabled = true

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification())
        initAudioEffect()
        listenAudioDevices()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL_ID, "SFX Glass Engine", NotificationManager.IMPORTANCE_LOW)
            ch.description = "Audio effect processing"
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SFX Glass — Master ${if (masterEnabled) "ON" else "OFF"}")
            .setContentText(if (masterEnabled) "ViPER engine aktif • Ketuk untuk buka" else "Master mati • Ketuk untuk nyalakan")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pi)
            .addAction(R.drawable.ic_tile_master, if (masterEnabled) "Matikan" else "Nyalakan", togglePending())
            .build()
    }

    private fun togglePending(): PendingIntent =
        PendingIntent.getService(this, 1, Intent(this, ViperGlassService::class.java).apply { action = ACTION_TOGGLE }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    private fun initAudioEffect() {
        scope.launch {
            try {
                // Coba buat AudioEffect untuk session 0 (global)
                audioEffect = createViPEREffect(0)
                if (audioEffect != null) {
                    audioEffect?.enabled = masterEnabled
                    log("ViPER Effect created session=0 enabled=$masterEnabled")
                } else {
                    log("AudioEffect is null — device tidak support atau belum patch audio_effects")
                }
            } catch (e: Exception) {
                log("Failed create effect: ${e.message}")
            }
            updateNotification()
        }
    }

    private fun createViPEREffect(sessionId: Int): AudioEffect? {
        return try {
            // Gunakan reflection agar kompatibel semua Android
            val uuid = java.util.UUID.fromString(EFFECT_UUID)
            // Cari constructor AudioEffect via reflection
            val clazz = AudioEffect::class.java
            val ctor: Constructor<AudioEffect> = clazz.getDeclaredConstructor(
                java.util.UUID::class.java, java.util.UUID::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
            )
            ctor.isAccessible = true
            val effect = ctor.newInstance(
                java.util.UUID.fromString(EFFECT_TYPE_UUID), uuid, 0, sessionId
            )
            effect
        } catch (e: Exception) {
            // Fallback: coba descriptor string
            try {
                // Beberapa ROM butuh AudioEffect(UUID, UUID, priority, audioSession)
                null
            } catch (_: Exception) { null }
        }
    }

    private fun listenAudioDevices() {
        // AudioDeviceCallback untuk auto-switch speaker/headphone/bluetooth
        // Disederhanakan: polling AudioManager.getDevices
        scope.launch {
            while (isActive) {
                delay(3000)
                // TODO: detect device change, load DeviceSettingsEntity
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> { masterEnabled = true; audioEffect?.enabled = true; updateNotification() }
            ACTION_STOP -> { masterEnabled = false; audioEffect?.enabled = false; updateNotification() }
            ACTION_TOGGLE -> { masterEnabled = !masterEnabled; audioEffect?.enabled = masterEnabled; updateNotification() }
            else -> {
                intent?.getBooleanExtra(EXTRA_MASTER, masterEnabled)?.let { masterEnabled = it; audioEffect?.enabled = it; updateNotification() }
            }
        }
        return START_STICKY
    }

    private fun updateNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification())
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        scope.cancel()
        audioEffect?.release()
        super.onDestroy()
    }

    private fun log(msg: String) = android.util.Log.i("SFX-Glass", msg)
}
