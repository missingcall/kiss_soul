package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/16 17:46.
 * @Describe
 */
@Parcelize
@Serializable
data class ReportTypeListModel(
    val informantTypeId: String,
    val informantTypeName: String
) : Parcelable