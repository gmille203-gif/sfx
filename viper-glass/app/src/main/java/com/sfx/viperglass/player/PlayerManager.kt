package com.sfx.viperglass.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(@ApplicationContext private val ctx: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set

    suspend fun connect() {
        val token = SessionToken(ctx, ComponentName(ctx, SFXPlayerService::class.java))
        controllerFuture = MediaController.Builder(ctx, token).buildAsync()
        controller = controllerFuture!!.await()
    }

    fun playUri(uri: String) {
        controller?.setMediaItem(MediaItem.fromUri(uri))
        controller?.prepare()
        controller?.play()
    }

    fun playList(uris: List<String>) {
        val items = uris.map { MediaItem.fromUri(it) }
        controller?.setMediaItems(items)
        controller?.prepare()
        controller?.play()
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture!!)
    }
}
