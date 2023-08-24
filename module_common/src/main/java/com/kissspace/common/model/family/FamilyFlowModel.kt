package com.kissspace.common.model.family

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/3 19:44.
 * @Describe
 */
@Serializable
@Parcelize
data class FamilyFlowModel(
    val current: Int,
    val hitCount: Boolean,
    val optimizeCountSql: Boolean,
    val pages: Int,
    @SerialName("records")
    val familyFlowRecords: List<FamilyFlowRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
) : Parcelable

@Serializable
@Parcelize
data class FamilyFlowRecord(
    val createTime: String,
    val displayId: String,
    val flowTotal: Double,
    val flowType: String,
    val nickname: String,
    val profilePath: String
): Parcelable
