package com.kissspace.dynamic

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/7/20 18:14.
 * @Describe
 */
@Serializable
@Parcelize
data class Dynamic(
    val createTime: Long? = null,
    val userName: String? = null,
) : Parcelable