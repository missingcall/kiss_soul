package com.kissspace.common.model

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
data class RoomBackgroundListBean(
    var backgroundName: String = "",
    var dynamicImage: String = "",
    var hadChosen: Boolean = false,
    var staticImage: String = "",
    var commodityId: String? = null
) : BaseObservable()