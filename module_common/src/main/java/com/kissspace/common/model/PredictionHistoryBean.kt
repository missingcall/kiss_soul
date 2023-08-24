package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class PredictionHistoryBean(
    val chatRoomId: String,
    val createTime: String,
    val inputIntegral: Int,
    val integralGuessId: String,
    val integralGuessTitle: String,
    val leftOption: String,
    val leftTimes: Double,
    val outputIntegral: Int,
    val rightOption: String,
    val rightTimes: Double,
    val state: String,
    val userId: String,
    val userIntegralGuessFlowId: String,
    val userOption: String,
    val creatorName: String,
    val creatorIcon: String,
    val creatorId: String
)