package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomIncomeBean(
    var today: String,
    var todayFlowTotal: Long = 0,
    var yesterday: String,
    var yesterdayFlowTotal: Long = 0
)
