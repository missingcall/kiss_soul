package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class CreateUserAccountBean(
    var userId:String,
    var displayId:String,
    var profilePath:String,
    var nickname:String,
    var mobile:String
) : Parcelable