package com.kissspace.common.model


@kotlinx.serialization.Serializable
data class MyVisitorResponse(
    val pages: Int,
    val records: List<VisitorListBean>,
    val total: Int
)

@kotlinx.serialization.Serializable
data class VisitorListBean(
    val charmLevel: Int,
    val consumeLevel: Int,
    val displayId: String,
    val nickname: String,
    val accid:String,
    val profilePath: String,
    val userId: String
)