package com.kissspace.common.model

import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/8/19 10:48.
 * @Describe
 */
@Serializable
data class DynamicMessageNotice(
    val interactiveMessages: Int,
    val likeMessage: Int
)