package com.kissspace.common.model.family

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author gaohangbo
 * @Date 2022/12/28 20:12.
 * @Describe
 */
@Parcelize
data class FamilyInformationModel(
    val familyAvatar: String,
    val familyDescribe: String,
    val familyNotice: String
) : Parcelable