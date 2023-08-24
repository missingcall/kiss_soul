package com.kissspace.common

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class AdolescentTimeBean(
    var userId:String,
    var time:Long
) : Parcelable