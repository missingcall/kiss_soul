package com.kissspace.common.model

import androidx.databinding.BaseObservable
import com.kissspace.common.config.Constants
import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/20 16:15
 * @Description: 排麦用户model
 *
 */
@Serializable
data class MicQueueUserModel(
    var userId: String,
    var displayId: String,
    var nickname: String,
    var sex: String = Constants.SEX_FEMALE,
    var profilePath: String,
    var wealthLevel: Int = 0,
    var charmLevel: Int = 0,
    var isChecked: Boolean = false
) : BaseObservable()