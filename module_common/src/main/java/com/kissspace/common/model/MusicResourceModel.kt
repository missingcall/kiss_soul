package com.kissspace.common.model

import androidx.databinding.BaseObservable

/**
 *@author: adan
 *@date: 2023/3/16
 *@Description:
 */
data class MusicResourceModel(
    var code : Int,
    var message:String,
    var data: MusicResourceInfoModel
)

data class MusicResourceInfoModel(
    var is_accompany:Int,
    var song_name:String,
    var singer_name:String,
    var duration:Int,
    var token_ttl:Int,
    var resources_size:Int,
    var resources:List<MusicResourceMessageModel>,
)

data class MusicResourceMessageModel(
    var resource_id :String,
    var size :Int,
    var quality:String
)
