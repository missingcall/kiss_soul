package com.kissspace.room.interfaces

import com.kissspace.common.model.MusicSongsInfoModel
import im.zego.zegoexpress.constants.ZegoPublisherState
import im.zego.zegoexpress.constants.ZegoUpdateType
import im.zego.zegoexpress.entity.ZegoStream
import org.json.JSONObject


interface MusicPlayListener {

    /**
     * 播放进度
     */
    fun onProgressUpdate(
        millisecond: Int,
    )

    /**
     * 播放歌曲
     */
    fun onPlayMusicUpdate(message: MusicSongsInfoModel)
}