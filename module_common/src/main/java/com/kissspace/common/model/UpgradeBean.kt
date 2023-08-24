package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/12 17:05
 * @Description: 版本更新
 *
 */
@kotlinx.serialization.Serializable
@Parcelize
data class UpgradeBean(
    val descript: String,
    val downUrl: String,
    val id: String,
    val intVersion: Int,
    val isForcedUpdate: Int,
    val operator: String,
    val os: Int,
    val version: String
):Parcelable