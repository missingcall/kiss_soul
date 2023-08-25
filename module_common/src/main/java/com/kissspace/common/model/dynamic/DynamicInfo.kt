package com.kissspace.common.model.dynamic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/8/5 11:28.
 * @Describe
 */
@Serializable
data class DynamicInfo(
    val countId: String?=null,
    val current: Int,
    val hitCount: Boolean,
    val maxLimit: Int?=null,
    val optimizeCountSql: Boolean,
    val pages: Int,
    @SerialName("records")
    val dynamicInfoRecords: List<DynamicInfoRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)
@Serializable
data class DynamicInfoRecord(
    val charmLevel: Int,
    val consumeLevel: Int,
    val createTime: String,
//    val dynamicCommentsList: Any,
    val dynamicId: String,
    val followRoomId: String,
    val followStatus: Boolean,
    val likeStatus: Boolean,
    val nickname: String,
    val numberOfLikes: Int,
    val pictureContentAuditStatus: String,
    val pictureDynamicContent: List<String>,
    val profilePath: String,
    val sex: String,
    val textContentAuditStatus: String,
    val textDynamicContent: String,
    val userId: String,
    val videoContentAuditStatus: String,
//    val videoDynamicContent: Any,
    val voiceContentAuditStatus: String,
    val voiceDynamicContent: String
)