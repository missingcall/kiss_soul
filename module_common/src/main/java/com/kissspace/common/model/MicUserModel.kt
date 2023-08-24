package com.kissspace.common.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import com.google.gson.annotations.SerializedName
import com.kissspace.common.config.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 16:02
 * @Description: 麦位用户model
 *
 */
@Serializable
@Parcelize
data class MicUserModel(
    var status: String? = "001",//001 进入排队列表  002上麦成功
    var onMicroPhoneNumber: Int = 0,//上麦序号
    var waitQueueNumber: Int = 0,//排麦人数
    var wheatPositionId: String = "",//麦位用户id
    var userRole: String = Constants.ROOM_USER_TYPE_NORMAL,//用户身份
    var wheatPositionIdName: String = "",//昵称
    var wheatPositionIdHeadPortrait: String = "",//头像
    var wheatPositionIdCharmValue: Long = 0,//魅力值
    var isShowIncome: String = "001",
    var isShowPkValue: Boolean = false,
    var pkValue: Long = 0,
    var lockWheat: Boolean = false,
    var checked: Boolean = false,
    var headWearIcon: String? = "",
    var headWearSvga: String? = "",
) : BaseObservable(), Parcelable