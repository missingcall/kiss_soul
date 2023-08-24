package com.kissspace.common.model.family

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author gaohangbo
 * @Date 2022/12/27 19:45.
 * @Describe
 */
@Parcelize
data class FamilyDetailModel(
    val familyName: String,
    val familyAvatar: String,
    val familyId: String,
    val familyNumber: String,
    val familyDesc: String,
    val familyState: String,
) : Parcelable