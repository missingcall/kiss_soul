package com.kissspace.common.model

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
data class RoomCollectListBean(
    var musicId: String = "",
    var checked :Boolean = false,
    var visibility: Boolean = false
) : BaseObservable()

@Serializable
data class RoomMusicCollectListBean(
    var records: List<MusicSongsInfoModel>, var total: Int, var pages: Int, var current: Int
)