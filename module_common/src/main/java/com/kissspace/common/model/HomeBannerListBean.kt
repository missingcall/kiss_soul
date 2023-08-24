package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class HomeBannerListBean(
    var nickname:String,
    var title:String,
    var desc: String,
    var url: String,
    var jump: String,
    //0是内链  1是外链
    var jumpType: String,
    var schema:String,
    var chatRoomId:String
)