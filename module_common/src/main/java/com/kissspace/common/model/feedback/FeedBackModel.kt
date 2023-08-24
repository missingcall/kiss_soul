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
class FeedBackModel(
    val typeDescribe: String? = null,
    val typeId: String? = null,
    val typeName: String? = null
) : Parcelable

