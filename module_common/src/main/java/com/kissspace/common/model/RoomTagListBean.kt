package com.kissspace.common.model

import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 房间标签列表返回实体
 *
 */
@Serializable
data class RoomTagListBean(var roomTagId: String, var roomTag: String, var state: String)