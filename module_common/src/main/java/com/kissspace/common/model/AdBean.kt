package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class AdBean(
    val adId: String,
    var displayTime: Int,
    val imgUrl: String,
    val link: String,
    val os: String,
    val state: String,
)