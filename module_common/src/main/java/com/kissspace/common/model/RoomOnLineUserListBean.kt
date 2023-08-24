package com.kissspace.common.model

import androidx.databinding.BaseObservable
import com.kissspace.util.ellipsizeString

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/16 15:23
 * @Description: 房间内在线用户列表
 *
 */
@kotlinx.serialization.Serializable
data class RoomOnLineUserListBean(
    val charmLevel: Int = 0,
    val displayId: String,
    val microPhone: Boolean = false,
    val nickname: String,
    val profilePath: String,
    var role: String,
    val userId: String,
    val wealthLevel: Int = 0,
    var isChecked: Boolean = false,
    var beautifulId: String = ""
) : BaseObservable() {

    fun getEllipsizeName() = nickname.ellipsizeString(7)

    fun getShowId() = beautifulId.ifEmpty { displayId }
}