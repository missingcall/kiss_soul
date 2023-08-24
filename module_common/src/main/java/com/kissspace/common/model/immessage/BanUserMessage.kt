package com.kissspace.common.model.immessage

import kotlinx.serialization.Serializable

@Serializable
data class BanUserMessage(var userId: String, var frozenDeadline: String, var reason: String)