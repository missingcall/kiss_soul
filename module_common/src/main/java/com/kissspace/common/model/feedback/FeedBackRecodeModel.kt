package com.kissspace.common.model.feedback

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/6 21:35.
 * @Describe
 */
@Parcelize
@Serializable
class FeedBackRecodeModel(
    val createTime: String,
    val feedbackDescribe: String,
    val feedbackId: String,
    val feedbackImage: List<String>?=null,
    val feedbackReply: String,
    val feedbackTypeId: String,
    val feedbackTypeName: String,
    val userId: String,
    //是否显示回复
    var isShowFeedBackReply:Boolean=true
) : Parcelable
