package com.kissspace.common.model


@kotlinx.serialization.Serializable
data class MyFollowResponse(
    val pages: Int,
    val records: List<FollowListBean>,
    val total: Int
)

@kotlinx.serialization.Serializable
data class FollowListBean(
    val charmLevel: Int,
    val consumeLevel: Int,
    val displayId: String,
    val followState: Boolean,
    val nickname: String,
    val profilePath: String,
    val userAttentionId: String,
    val userId: String
)