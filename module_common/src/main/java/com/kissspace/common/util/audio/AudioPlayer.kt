package com.kissspace.common.util.audio

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:42.
 * @Describe
 */
object AudioPlayer :MediaPlayer.OnPreparedListener, DefaultLifecycleObserver {

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val handler = Handler(Looper.getMainLooper())

    var playerState = PlayerState.STOPPED

    var playerCallback: PlayerCallback?=null

    private var prevPosMills: Long = 0

    private const val PLAYING_INTERVAL = 1000L

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
        mediaPlayer.seekTo(0)
        playerState = PlayerState.PLAYING
        playerCallback?.onStartPlay()
        mediaPlayer.setOnCompletionListener {
            stopPlayer()
        }
        schedulePlaybackTimeUpdate()
    }

    private fun schedulePlaybackTimeUpdate() {
        handler.postDelayed({
            try {
                if (playerState == PlayerState.PLAYING) {
                    var pos = mediaPlayer.currentPosition.toLong()
                    if (pos < prevPosMills) {
                        pos = prevPosMills
                    } else {
                        prevPosMills = pos
                    }
                    playerCallback?.onPlayProgress(pos/1000)
                }
                schedulePlaybackTimeUpdate()
            } catch (e: IllegalStateException) {
                logE("Player is not initialized!")
                playerCallback?.onError(e.message.toString())
            }
        }, PLAYING_INTERVAL)
    }
    private fun restartPlayer(dataSource: String) {
        try {
            playerState = PlayerState.STOPPED
            mediaPlayer.reset()
            mediaPlayer.setDataSource(dataSource)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            logE(e)
            playerCallback?.onError(e.toString())
        }
    }
    fun play(filePath: String) {
        try {
            if (playerState != PlayerState.PLAYING) {
                restartPlayer(filePath)
                try {
                    mediaPlayer.setOnPreparedListener(this)
                    mediaPlayer.prepareAsync()
                } catch (ex: IllegalStateException) {
                    logE(ex)
                    restartPlayer(filePath)
                    mediaPlayer.setOnPreparedListener(this)
                    try {
                        mediaPlayer.prepareAsync()
                    } catch (e: IllegalStateException) {
                        logE(e)
                        restartPlayer(filePath)
                    }
                }
            }
        } catch (e: IllegalStateException) {
            logE("Player is not initialized!")
        }
    }
     fun stopPlayer() {
        stopPlaybackTimeUpdate()
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setOnCompletionListener(null)
        playerCallback?.onStopPlay()
        playerState = PlayerState.STOPPED
        prevPosMills = 0
    }

    private fun stopPlaybackTimeUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stopPlayer()
    }

}