package com.kissspace.common.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 房间列表返回实体
 *
 */
@Serializable
@Parcelize
data class RoomListBean(
    var crId: String = "",
    var userId: String = "",
    var roomTitle: String = "",
    var roomIcon: String = "",
    var userName: String = "",
    var backgroundUrl: String = "",
    var roomTagUrl: String = "",
    var onlineNum: Int = 0,
    var roomBeautifulId: Boolean = false,
    var isRoomPwd: Boolean = false,
    var banner: MutableList<RoomListBannerBean> = mutableListOf(),
    var wheatPositionList: List<MicUserModel> = mutableListOf(),
    var chatRoomId: String = "",
    var jumpType: Int = 0,
    var roomAffiche: String = "",
    var chatRoomHeat: Long = 0
) : BaseObservable(),Parcelable

@Serializable
data class RoomListResponse(
    var records: List<RoomListBean>, var total: Int, var pages: Int, var current: Int
)