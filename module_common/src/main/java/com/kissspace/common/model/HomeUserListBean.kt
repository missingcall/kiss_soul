package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/4
 * @Description:
 *
 */
@kotlinx.serialization.Serializable
@Parcelize
data class HomeUserListBean(
    var userId: String,
    var displayId:String,
    var nickname:String,
    var birthday:String,
    var sex:String = "",
    var profilePath:String,
    var personalSignature:String,
    var bgPath:String?,
    var stayRoomId:String?,
): Parcelable