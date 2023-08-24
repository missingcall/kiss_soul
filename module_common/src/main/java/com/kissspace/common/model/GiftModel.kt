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
data class GiftModel(
    val id: String,
    var name: String,
    val giftOrBox: String = "001",
    val isPackGift: Boolean = false,
    val boxType: String = "001",
    val price: Int,
    val svg: String = "",
    val url: String,
    var checked: Boolean = false,
    var isDark: Boolean = false,
    var freeNumber: Int = 0,
    var info: String = "",
    var introductionUrl: String = "",
    var index: Int = 0,
    var num: Int = 0,
    val locked: Boolean = false,
    var isPrivilegeGift: Boolean = false,
) : BaseObservable(), Parcelable