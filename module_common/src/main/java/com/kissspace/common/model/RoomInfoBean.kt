package com.kissspace.common.model

import android.os.Parcelable
import com.noober.background.view.Const
import com.kissspace.common.config.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13
 * @Description: 房间信息返回实体
 *
 */
@Serializable
@Parcelize
data class RoomInfoBean(
    var collections: Boolean = false,
    val crId: String,
    var userRole: String,
    val houseOwnerId: String,
    val houseOwnerName: String,
    val houseOwnerProfilePath: String,
    val neteaseChatId: String,
    var roomPwd: String = "",
    var roomAffiche: String = "",
    var roomIcon: String = "",
    val zegoTokcen: String,
    val roomTagId: String,
    var roomTitle: String,
    var beautifulId: String = "",
    val showId: String,
    val roomTagCategory: String = Constants.ROOM_TYPE_PARTY,
    var backgroundUrl: String = "",
    var charmOnOff: String = "001",
    var zeGoPushUrl: String = "",
    var zeGoPullUrl: String = "",
    var screenStatus: String = "",//横竖屏，001 竖屏   002 横屏
    var wheatPositionList: MutableList<MicUserModel> = mutableListOf(),
    var backgroundDynamicImage: String = "",
    var backgroundStaticImage: String = "",
    val largeScreenMessageResponse: RoomScreenMessageModel? = null
) : Parcelable