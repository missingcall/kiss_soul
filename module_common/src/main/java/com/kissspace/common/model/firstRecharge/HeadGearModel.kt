package com.kissspace.common.model.firstRecharge

import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/2/23 14:43.
 * @Describe 头环
 */
@Serializable
class HeadGearModel(
    val icon: String,
    val name: String,
    //天数
    val headerIndate:Double
)