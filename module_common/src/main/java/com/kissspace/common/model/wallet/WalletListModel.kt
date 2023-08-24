package com.kissspace.common.model.wallet

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize

/**
 * @Author gaohangbo
 * @Date 2022/12/29 21:02.
 * @Describe
 */
@Parcelize
data class WalletListModel(
    val exchangeNumber: Double,
    val rmbNumber: Double,
    var isWalletSelected: Boolean = false,
) : Parcelable,BaseObservable()