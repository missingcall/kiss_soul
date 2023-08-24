package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author gaohangbo
 * @Date 2023/1/30 16:31.
 * @Describe
 */
//任务状态 001 待解锁 002 进行中 003 待领取 004 已领取	string
//rewardType	奖励类型 001积分 002 道具 003 礼包盲盒 以及对应的奖励数量 json串
@kotlinx.serialization.Serializable
@Parcelize
data class TaskRewardModel(
    val conditional: String?=null,
    val finishStatus: String?=null,
    var rewardType: String?=null,
    val taskIcon: String?=null,
    val taskId: String?=null,
    val validTime: Long?=0,
): Parcelable

//data class RewardModelCount(val:)