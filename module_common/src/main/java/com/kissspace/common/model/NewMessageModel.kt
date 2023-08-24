package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class NewMessageModel(
    val activityCenterMessage: Int?=null,
    val familyMessage: Int?=null,
    val feedbackMessage: Int?=null,
    val taskCenterMessage: Int?=null,
    val walletMessage: Int?=null,
)