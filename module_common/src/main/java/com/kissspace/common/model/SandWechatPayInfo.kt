package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SandWechatPayInfo(var pay_extra: SandWechatPayExtra)

@Serializable
data class SandWechatPayExtra(
    var wx_app_id: String,
    val gh_ori_id: String,
    var path_url: String,
    var miniProgramType: String
)