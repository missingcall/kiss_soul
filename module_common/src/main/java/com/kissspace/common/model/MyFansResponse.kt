package com.kissspace.common.model

import androidx.databinding.BaseObservable


@kotlinx.serialization.Serializable
data class MyFansResponse(
    val pages: Int,
    val records: List<FansListBean>,
    val total: Int
)

@kotlinx.serialization.Serializable
data class FansListBean(
    val charmLevel: Int,
    val consumeLevel: Int,
    val displayId: String,
    var followState: Boolean,
    val nickname: String,
    val profilePath: String,
    val userAttentionId: String,
    val userId: String,
) : BaseObservable()