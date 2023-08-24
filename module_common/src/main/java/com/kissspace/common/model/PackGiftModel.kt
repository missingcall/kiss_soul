package com.kissspace.common.model

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/24 10:02
 * @Description: 背包礼物实体类
 *
 */
@Serializable
data class PackGiftModel(
    var id: String,
    var giftId: String,
    var giftName: String,
    var giftIcon: String,
    var price: Int,
    var num: Int,
) : BaseObservable()