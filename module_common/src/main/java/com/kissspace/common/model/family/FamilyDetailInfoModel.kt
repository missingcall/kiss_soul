package com.kissspace.common.model.family

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/3 19:44.
 * @Describe
 */
@Serializable
@Parcelize
data class FamilyDetailInfoModel(
    val createTime: Long? = null,
    val creatorId: String? = null,
    val creatorPhone: String? = null,
    val displayId: Int? = null,
    val familyDesc: String? = null,
    val familyHeadId: String? = null,
    val familyHeadName: String? = null,
    val familyIcon: String? = null,
    val familyId: String? = "",
    val familyName: String? = null,
    val familyNotice: String? = null,
    // 001 正常；002 解散；003 申请解散
    val familyStatus: String? = null,
    val userFamilyStatus: String? = null,
    val familyUserNum: Int? = null,
    val roomRevenue: Double? = null,
    val updateTime: Long? = null,
    val userRevenue: Double? = null,
    val chatRoomId: String? = null,
    var isFamilyHeader: Boolean = false,
    var isFamilyAdmin: Boolean = false,
    val thisWeekInLicenseRoomRevenue: Double? = null,
    val thisWeekOutLicenseRoomRevenue: Double? = null,
    val todayInLicenseRoomRevenue: Double? = null,
    val todayOutLicenseRoomRevenue: Double? = null,
    val isHaveUserApply: Boolean? = false
) : Parcelable
