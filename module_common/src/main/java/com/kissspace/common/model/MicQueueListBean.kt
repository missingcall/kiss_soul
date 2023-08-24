package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class MicQueueListBean(
    var userId: String,
    var displayId: String,
    var nickname: String,
    var sex: String,
    var profilePath: String,
)