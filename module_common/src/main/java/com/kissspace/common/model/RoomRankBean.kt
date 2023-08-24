package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class RoomRankBean(
    val currentUser: RoomRankCurrentUser,
    val index: Int = -1,
    val isOnRank: Boolean,
    val rankUserList: List<RoomRankUser>
)

@kotlinx.serialization.Serializable
data class RoomRankCurrentUser(
    val charmLevel: Int,
    val consumeLevel: Int,
    val difference: Int = 0,
    val nickname: String,
    val profilePath: String,
    val userId: String,
    val value: Long = 0
)

@kotlinx.serialization.Serializable
data class RoomRankUser(
    val charmLevel: Int,
    val consumeLevel: Int,
    val nickname: String,
    val profilePath: String,
    val userId: String,
    val value: Long,
)