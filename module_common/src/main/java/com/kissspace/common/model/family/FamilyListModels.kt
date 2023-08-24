package com.kissspace.common.model.family

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2022/12/30 11:39.
 * @Describe
 */
@Serializable
@Parcelize
data class FamilyListModels(
    val createTime: String?=null,
    val creatorId: String,
    val creatorPhone: String,
    val displayId: Int=0,
    val familyDesc: String? =null,
    val familyHeadId: String,
    val familyHeadName: String,
    val familyIcon: String?=null,
    val familyId: String,
    val familyName: String,
    val familyNotice: String? = null,
    val familyStatus: String,
    val familyUserNum: Int?=null,
    val roomRevenue: Double?=null,
    val updateTime: String?=null,
    val userFamilyStatus: String,
    val userRevenue: Double?=null,
) : Parcelable {
    fun getDefaultFamilyDesc() : String{
        return if (familyDesc.isNullOrEmpty()){
            "该家族还没有家族介绍"
        }else {
            familyDesc
        }
    }
}