package com.kissspace.common.util.audio

/**
 * @Author gaohangbo
 * @Date 2023/7/27 16:02.
 * @Describe
 */
interface PlayerCallback {
    fun onStartPlay()
    fun onPlayProgress(mills: Long)
    fun onStopPlay()
    fun onError(throwable: String)
}
enum class PlayerState {
    STOPPED,
    PLAYING
}