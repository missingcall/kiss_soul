package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class PredictionRankingBean(
    val nickname: String = "",
    val profilePath: String = "",
    val profit: Double = 0.0,
    val ranking: Int = 0,
    val rankingList: List<PredictionRankingListBean> = mutableListOf()
)

@kotlinx.serialization.Serializable
data class PredictionRankingListBean(
    val nickname: String,
    val profilePath: String,
    val profit: Double,
    val userId: String
)