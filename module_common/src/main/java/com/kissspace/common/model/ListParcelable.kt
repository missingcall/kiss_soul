package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class ListParcelable<T : Parcelable>(var value: List<T>?) : Parcelable, Serializable

@Parcelize
data class LongListParcelable(var value: List<Long>?) : Parcelable, Serializable