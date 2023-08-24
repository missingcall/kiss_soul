package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomBlackListUserBean(
    val profilePath: String,
    val consumeLevel: Int,
    val charmLevel: Int,
    val userId: String,
    val displayId: String = "",
    val beautifulId: String = "",
    val nickname:String,
){
    fun getId():String = beautifulId.ifEmpty { displayId }
}