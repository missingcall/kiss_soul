package com.kissspace.common.model.wallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2022/12/27 19:45.
 * @Describe
 */
@Parcelize
@Serializable
data class WalletModel(
    val accountBalance: Double? = null,
    val coin: Double? = null,
    val diamond: Double? = null,
    //	身份（001-普通用户，002-主播，003-家族长）
    val identity: String,
    val isBindAliPay: Boolean,
) : Parcelable
