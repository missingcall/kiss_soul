package com.kissspace.common.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/16 14:17
 * @Description: 普通礼物实体类
 *
 */
@Parcelize
@Serializable
data class NormalGiftModel(
    val id: String,
    var name: String,
    val giftOrBox: String,
    val boxType: String = "001",
    val price: Int,
    val svg: String = "",
    val url: String,
    var checked: Boolean = false,
    var isDark: Boolean = false,
    var freeNumber: Int,
    var info: String = "",
    var introductionUrl: String = "",
    var index: Int = 0,
    var isPrivilegeGift: Boolean,
) : BaseObservable(), Parcelable