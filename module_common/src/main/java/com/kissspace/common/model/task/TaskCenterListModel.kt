package com.kissspace.common.model.task

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/9 20:21.
 * @Describe 任务中心
 */

@Parcelize
@Serializable
data class TaskCenterListModel(
    val dailyTaskInfoList: List<DailyTaskInfo>,
    val noviceTaskInfoList: List<NoviceTaskInfo>
) : Parcelable

@Parcelize
@Serializable
data class DailyTaskInfo(
    val conditional: String,
    val createTime: String,
    val cycle: Int,
    val finishStatus: String,
    val schema: String,
    val jumpLink: String,
    val jumpType: Int? = null,
    val maxAcceptNum: Int,
    val openTime: String,
    val rewardType: String? = null,
    val status: String,
    val taskDesc: String,
    val taskIcon: String? = null,
    val taskId: String,
    val taskModel: String,
    val taskName: String,
    val taskType: String,
    val updateTime: String
) : Parcelable

@Parcelize
@Serializable
data class NoviceTaskInfo(
    val conditional: String,
    val createTime: String,
    val cycle: Int,
    val finishStatus: String,
    val schema: String,
    val jumpLink: String,
    //内链 0 应用内页面 外链 1 web页面
    val jumpType: Int? = null,
    val maxAcceptNum: Int,
    val openTime: String,
    val rewardType: String? = null,
    val status: String,
    val taskDesc: String,
    val taskIcon: String? = null,
    val taskId: String,
    val taskModel: String,
    val taskName: String,
    val taskType: String,
    val updateTime: String
) : Parcelable