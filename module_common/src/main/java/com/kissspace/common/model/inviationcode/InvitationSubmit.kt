package com.kissspace.common.model.inviationcode

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/3/1 10:37.
 * @Describe
 */
@Serializable
data class InvitationSubmit(
    val isAutoWear: Boolean
)
