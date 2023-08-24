package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class MyCollectResponse(
    val pages: Int,
    val records: List<CollectListBean>,
    val total: Int
)

@kotlinx.serialization.Serializable
data class CollectListBean(
    val chatRoomId: String,
    val collectId: String,
    val roomIcon: String,
    val roomAffiche: String = "",
    val roomTitle: String,
    val showId: String,
    val type: String,
    val userId: String
)