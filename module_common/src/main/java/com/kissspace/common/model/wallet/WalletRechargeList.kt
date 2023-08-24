package com.kissspace.common.model.wallet

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/12 15:52.
 * @Describe 充值列表
 */
@Parcelize
@Serializable
data class WalletRechargeList(
    val createTime: String?=null,
    val payChannelKey: String,
    val payChannelMaxAmount: Int,
    val payChannelMinAmount: Int,
    val payChannelName: String,
    val payChannelPackageName: String?=null,
    val payChannelSort: Int,
    val payChannelType: String,
    val payProductListResponses: List<PayProductResponses>?=null,
    val firstRechargePayProductListResponses:List<FirstRechargePayProductListResponses>,
    val updateTime: String?=null,
    var isSelected: Boolean = false
) : Parcelable,BaseObservable()
@Parcelize
@Serializable
data class PayProductResponses(
    val createTime: String?=null,
    val payChannelType: String,
    val payProductCash: Double,
    val payProductGoldCoin: Int,
    val payProductId: String,
    val payProductIsHot: Int,
    val payProductSort: Int,
    val updateTime: String?=null,
    var isSelected: Boolean =false
): Parcelable,BaseObservable()


@Parcelize
@Serializable
data class FirstRechargePayProductListResponses(
    val createTime: String?=null,
    val payChannelType: String,
    val payProductCash: Double,
    //{\"002\":[{\"giftId\":\"377d2c2376be07a359e627c7667f563b\",\"giftNum\":1,\"giftName\":\"生日蛋糕\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E7%94%9F%E6%97%A5%E8%9B%8B%E7%B3%95-1675389045872.png\"},{\"giftId\":\"8bb9d30212997806e9bd4d3a85b50a9b\",\"giftNum\":1,\"giftName\":\"木马\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E6%97%8B%E8%BD%AC%E6%9C%A8%E9%A9%AC-1672214371685.png\"},{\"giftId\":\"204\",\"giftNum\":1,\"giftName\":\"抖音一号\",\"giftImg\":\"https://macrodjs-oos.oss-cn-hangzhou.aliyuncs.com/admin/gift/images/%E9%9F%B3%E6%B4%BE_%E7%A4%BC%E7%89%A9%E5%9B%BE%20%2819%29-1673863091846.png\"}]}
    val payProductGiftCoin: String,
    val payProductGoldCoin: Double?=null,
    val payProductId: String,
    val payProductIsFirstRecharge: Int,
    val payProductIsHot: Int,
    val payProductSort: Int,
    val updateTime: String?=null
): Parcelable,BaseObservable()



