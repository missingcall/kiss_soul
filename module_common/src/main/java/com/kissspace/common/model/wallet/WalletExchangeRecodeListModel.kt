package com.kissspace.common.model.wallet

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2022/12/29 21:02.
 * @Describe
 */
@Serializable
@Parcelize
data class WalletExchangeRecodeListModel(
    val current: Int,
    val hitCount: Boolean,
    val optimizeCountSql: Boolean,
    val pages: Int,
    @SerialName("records")
    val walletExchangeRecodeList: List<WalletExchangeRecode>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
) : Parcelable
@Serializable
@Parcelize
data class WalletExchangeRecode(
    val createTime: String,
    val orderNo: String,
    //"下发渠道001：银行卡，002：支付宝"
    val distributionChannel:String?=null,
    val remark: String?=null,
    val status: Int,
    val trueName: String,
    val updateTime: String?=null,
    val userId: String,
    val withdrawCardNo: String,
    val withdrawCash: Double,
    val withdrawCoin: Double,
    val withdrawFee: Double,
    val withdrawFinalCash: Double,
    val withdrawType: Int
) : Parcelable
