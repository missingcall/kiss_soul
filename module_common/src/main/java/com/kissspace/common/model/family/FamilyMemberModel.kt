package com.kissspace.common.model.family

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/1/4 11:50.
 * @Describe
 */

@Serializable
@Parcelize
data class FamilyMemberModel(
    val current: Int,
    val pages: Int,
    @SerialName("records")
    val familyMemberRecords: List<FamilyMemberRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
) : Parcelable

@Serializable
@Parcelize
data class FamilyMemberRecord(
    val charmLevel: Int? = null,
    val consumeLevel: Int? = null,
    val displayId: String? = null,
    val mobile: String? = null,
    val nickname: String? = null,
    val profilePath: String? = null,
    val userId: String? = null,
    val isFamilyHeader: Boolean? = null,
    var isSelected: Boolean = false,
    var isShowSettingManager: Boolean = false,
    var userRole: String = "001",//001家族成员，003管理员，004家族长,
    var isShowMoveOut: Boolean = false
) : Parcelable, BaseObservable() {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FamilyMemberRecord) {
            return false
        }
        val model: FamilyMemberRecord = other
        if (model === this) {
            return true
        }
        return userId == model.userId
    }
}
