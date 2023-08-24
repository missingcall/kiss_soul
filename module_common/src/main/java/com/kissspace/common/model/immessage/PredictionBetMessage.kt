package com.kissspace.common.model.immessage

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/9 10:27
 * @Description: 下注消息
 *
 */
data class PredictionBetMessage(
    var userId: String,
    var integralGuessId: String,
    var leftBetAmount: Int,
    var rightBetAmount: Int,
    var leftBetNum: Int,
    var rightBetNum: Int,
    var leftTimes: Double,
    var rightTimes: Double
)