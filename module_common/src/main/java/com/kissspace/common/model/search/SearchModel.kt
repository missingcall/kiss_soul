package com.kissspace.common.model.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/19 15:36.
 * @Describe
 */
@Parcelize
@Serializable
data class SearchModel(
    val chatRoomResponsePage: ChatRoomResponsePage,
    val userResponsePage: UserResponsePage
) : Parcelable

@Parcelize
@Serializable
data class ChatRoomResponsePage(
    val countId: String? = null,
    val current: Int,
    val hitCount: Boolean,
    val maxLimit: Int? = null,
    val optimizeCountSql: Boolean,
    val orders: List<String>? = null,
    val pages: Int,
    @SerialName("records")
    val chatRoomList: List<SearchRecord>? = null,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
) : Parcelable

@Parcelize
@Serializable
data class UserResponsePage(
    val countId: String? = null,
    val current: Int,
    val hitCount: Boolean,
    val maxLimit: Int? = null,
    val optimizeCountSql: Boolean,
    val orders: List<String>? = null,
    val pages: Int,
    @SerialName("records")
    val userRecords: List<SearchRecord>? = null,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
) : Parcelable

@Parcelize
@Serializable
data class SearchRecord(
    //房间model
    val crId: String? = null,
    val isRoomPwd: Boolean? = null,
    val roomIcon: String? = null,
    val roomTitle: String? = null,
    //用户model
    val charmLevel: Int = 0,
    val consumeLevel: Int = 0,
    val nickName: String? = null,
    val profilePath: String? = null,
    val userId: String? = null,

    val showId: String? = null,
    //靓号
    val beautifulId: String? = null,
) : Parcelable