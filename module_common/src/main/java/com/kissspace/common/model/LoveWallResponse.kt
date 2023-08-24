package com.kissspace.common.model

import com.kissspace.common.util.getFriendlyTimeSpanByNow
import com.kissspace.util.millis2String
import kotlin.math.ceil

@kotlinx.serialization.Serializable
data class LoveWallResponse(
    val current: Int,
    val giveGiftRecordList: List<LoveWallListBean>,
    val recordTotal: Int
)

@kotlinx.serialization.Serializable
data class LoveWallListBean(
    val chatRoomId: String = "",
    val chatRoomTitle: String = "",
    val createTime: Long,
    val giftId: String,
    val giftName: String,
    val number: Int,
    val recodeType: String,
    val price: Int,
    val sourceUserId: String,
    val sourceUserNickname: String,
    val sourceUserProfilePath: String,
    val targetUserId: String,
    val targetUserNickname: String,
    val targetUserProfilePath: String,
    val totalValue: Long,
    val url: String,
    val showTime: String = getFriendlyTimeSpanByNow(createTime)
)

private fun formatTime(time: Long): String {
    return when (val timeDiff = System.currentTimeMillis() - time) {
        in 0..60 * 60 * 1000 -> ceil(timeDiff.toDouble().div(60 * 1000)).toInt()
            .toString() + "分钟前"

        in 60 * 60 * 1000..24 * 60 * 60 * 1000 -> ceil(
            timeDiff.toDouble().div(60 * 60 * 1000)
        ).toInt().toString() + "小时前"

        in 24 * 60 * 60 * 1000..7 * 24 * 60 * 60 * 1000 -> ceil(
            timeDiff.toDouble().div(24 * 60 * 60 * 1000)
        ).toInt().toString() + "天前"

        else -> time.millis2String()
    }
}