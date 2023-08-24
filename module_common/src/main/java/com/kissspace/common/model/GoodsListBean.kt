package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/4 20:31
 * @Description: 商城列表实体
 *
 */
@kotlinx.serialization.Serializable
@Parcelize
data class GoodsListBean(
    val commodityId: String,
    val commodityInfoId: String,
    val commodityName: String,
    val commodityType: String,
    val icon: String,
    val price: Int = 0,
    val svga: String = "",
    val timeLimit: Int = 0,
    val pointsPrice: Long? = null,
    val coinPrice: Long? = null,
    val description: String = "",
    val attribute: String
) : Parcelable