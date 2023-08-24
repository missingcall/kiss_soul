package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class BlackListBean(
    var charmLevel: Int,
    var consumeLevel: Int,
    var displayId: String,
    var nickname: String,
    var profilePath: String,
    var userId: String
)