package com.kissspace.common.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class UserAccountBean(
    var token:String?,
    var tokenHead:String?,
    var registrationIcon:String,
    var userId:String,
    var profilePath:String,
    var nickname:String,
    var displayId:String,
    var phone:String = "",
    var visibility: Boolean = true,
    var checked :Boolean = false
) : Parcelable, BaseObservable() {
    fun getId() : String{
        return "ID：$displayId"
    }
}