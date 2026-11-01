package com.sfx.viperglass.util

import android.media.audiofx.AudioEffect
import android.util.Log

/**
 * Universal AudioEngine — handle both legacy HIDL and new AIDL HAL
 * Jika detect AIDL, beri warning dan arahkan ke module AIDL (future)
 */
object AudioEngine {
    fun isAIDL(): Boolean {
        return try {
            val s = Runtime.getRuntime().exec("service list").inputStream.bufferedReader().readText()
            s.contains("android.hardware.audio.effect.IFactory")
        } catch (_: Exception) { false }
    }

    fun probeViPER(): Boolean {
        return try {
            // Cek apakah audio_effects sudah patch
            val configs = arrayOf("/vendor/etc/audio_effects.xml","/system/etc/audio_effects.conf","/odm/etc/audio_effects.xml")
            configs.any { f -> try { java.io.File(f).readText().contains("v4a_standard_re") } catch (_:Exception){ false } }
        } catch (_:Exception){ false }
    }

    fun createEffect(session: Int): AudioEffect? {
        return try {
            val uuid = java.util.UUID.fromString("90380da3-8536-4744-a6a3-5731970e640f")
            val type = java.util.UUID.fromString("e0e904a0-5451-11e2-bcfd-0800200c9a66")
            AudioEffect(type, uuid, 0, session)
        } catch (e: Exception) {
            Log.e("SFX-Glass", "createEffect failed: ${e.message}")
            null
        }
    }
}
