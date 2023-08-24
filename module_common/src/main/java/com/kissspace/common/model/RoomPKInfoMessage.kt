package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomPKInfoMessage(
    var blueTeamDto: TeamPKInfo,
    var redTeamDto: TeamPKInfo,
    var countdown: Long,
    var microphonePkValueDtoList: List<TeamPKValueInfo>
)

@Serializable
data class TeamPKInfo(
    var totalPkValue: Long,
    var userList: List<TeamPKUserInfo>,
)

@Serializable
data class TeamPKUserInfo(var profilePath: String, var boostValue: Double)

@Serializable
data class TeamPKValueInfo(var microphonePkValue: Long, var microphonePosition: Int)