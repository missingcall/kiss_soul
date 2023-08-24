package com.kissspace.common.model.inviationcode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/2/2 11:16.
 * @Describe
 */
@Serializable
@Parcelize
data class InvitationModel(
    val chatRoomIds: List<String>,
    val day: Int,
    val headwearName: String,
    val headwearUrl: String,
    val mysteryBoxName: String,
    val mysteryBoxUrl: String,
    val isPopUp: Boolean
) : Parcelable
