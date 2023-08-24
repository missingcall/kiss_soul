package com.kissspace.common.model.wallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author gaohangbo
 * @Date 2023/1/11 14:49.
 * @Describe 用户的钱包model
 */
@Parcelize
data class WalletUserModel(
    val userId: String,
    val nickName: String,
    val userIcon:String
) : Parcelable