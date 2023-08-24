package com.kissspace.common.model

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

/**
 *@author: adan
 *@date: 2023/3/16
 *@Description:
 */
data class MusicListModel(
    var code : Int,
    var message:String,
    var data: MusicSongsModel
)
data class MusicSongsModel(
    var songs:List<MusicSongsInfoModel>
)

@Serializable
data class MusicSongsInfoModel(
    var song_id:String ="",
    var song_name:String="",
    var singer_name:String="",
    var duration:Int=0,
    var songId:String="",
    var songName:String="",
    var singerName:String="",
    var checked :Boolean = false,
    var visibility :Boolean = false,
    var isCollect:Boolean = true,
    var copyright:MusicCopyRightModel? = null,
):BaseObservable() {
    fun getSongsId() :String{
        return song_id.ifEmpty { songId }
    }
    fun getSongsName() :String{
        return song_name.ifEmpty { songName }
    }
    fun getSingersName():String{
        return singer_name.ifEmpty { singerName }
    }
}

@Serializable
data class MusicCopyRightModel(
    var song_lyric :Int,
    var recording :Int,
    var channel:Int
)
