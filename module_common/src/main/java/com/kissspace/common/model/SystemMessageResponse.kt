package com.kissspace.common.model

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
data class SystemMessageResponse(
    var size: Int, var total: Int, var records: List<SystemMessageModel>
)

@Serializable
data class SystemMessageModel(
    var content: String,
    var coverPicture: String = "",
    var hyperlink: String = "",
    var messageFormat: String,
    var sendTime: Long,
    var title: String = "",
    var unReadCount: Int = 0,
) : BaseObservable()