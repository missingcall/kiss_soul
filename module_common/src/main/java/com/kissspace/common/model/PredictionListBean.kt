package com.kissspace.common.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 16:33
 * @Description: 积分预言记录model
 *
 */
@Serializable
@Parcelize
data class PredictionListBean(
    val chatRoomId: String,
    val createTime: Long,
    val creatorId: String,
    val integralGuessId: String,
    val integralGuessTitle: String,
    val leftOption: String,
    var leftTimes: Double = 0.0,
    val rightOption: String,
    var rightTimes: Double = 0.0,
    var state: String = "001",
    var validTime: Long = System.currentTimeMillis(),
    var leftTime: Long = validTime - System.currentTimeMillis(),
    var leftBetNum: Int = 0,
    var rightBetNum: Int = 0,
    var leftBetAmount: Int = 0,
    var rightBetAmount: Int = 0,
    var whichWin: String = "000",
    var userBetOption: String = "",
    var isShowTitle: Boolean = false,
    var title: String = "",
) : BaseObservable(), Parcelable