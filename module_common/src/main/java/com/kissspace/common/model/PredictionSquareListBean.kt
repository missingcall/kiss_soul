package com.kissspace.common.model

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/4 13:11
 * @Description: 预言广场列表实体类
 *
 */
@kotlinx.serialization.Serializable
data class PredictionSquareListBean(
    val chatRoomId: String,
    val createTime: String,
    val creatorId: String,
    val creatorNickName: String,
    val integralGuessId: String,
    val integralGuessTitle: String = "",
    val leftBetAmount: Int,
    val leftBetNum: Int,
    val leftOption: String,
    val leftTimes: Float,
    val rightBetAmount: Int,
    val rightBetNum: Int,
    val rightOption: String,
    val rightTimes: Float,
    val roomIcon: String,
    val roomTitle: String,
    val updateTime: String,
    val validTime: Long,
    val totalBetNum: Int = leftBetNum + rightBetNum,
)