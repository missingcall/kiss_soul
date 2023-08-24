package com.kissspace.common.model.immessage

data class CreateBetMessage(
    var integralGuessId: String,
    var integralGuessTitle: String,
    var validTime: Long,
    var leftProgress: Int,
    var rightProgress: Int,
    var leftTime: Long = 0,
)