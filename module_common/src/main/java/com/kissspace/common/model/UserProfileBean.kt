package com.kissspace.common.model

import com.kissspace.common.config.Constants
import com.kissspace.util.logE
import java.util.*


/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/7 11:08
 * @Description: 用户主页信息实体
 *
 */
@kotlinx.serialization.Serializable
data class UserProfileBean(
    val bgPath: MutableList<String> = mutableListOf(),
    var birthday: String = "2000-01-01",
    val car: List<PersonCar> = mutableListOf(),
    val charmLevel: Int = 0,
    val consumeLevel: Int = 0,
    val displayId: String,
    val beautifulId: String = "",
    val giftWall: List<CommonGiftInfo>,
    val nickname: String,
    val auditingNickname: String = "",
    val accid: String,
    val personalSignature: String = "",
    val auditingPersonalSignature: String = "",
    var profilePath: String,
    val profilePathAuditStatus: String,
    val sex: String = Constants.SEX_FEMALE,
    val userId: String,
    var attention: Boolean,
    var auditingProfilePath: String = "",
    var auditingBgPath: List<String> = mutableListOf(),
    var pulledBlack: Boolean,
    var bePulledBlack: Boolean,
    val followRoomId: String = "",
    val followRoomIcon: String = "",
    val followRoomName: String = "",
    val familyName: String = "",
    val medalList: MutableList<UserMedalBean> = mutableListOf()
) {
    fun getActualPersonalSignature(): String {
        return if (auditingPersonalSignature.isNullOrEmpty()) personalSignature else auditingPersonalSignature
    }

    fun getAllPicture(): MutableList<String> {
        val list = mutableListOf<String>()
        list.addAll(bgPath)
        list.addAll(auditingBgPath)
        return list
    }

    fun getShowId() = beautifulId.ifEmpty { displayId }

    fun getAge(): String {
        var year = 2000
        if (birthday != "2000-01-01") {
            val str = birthday.split("-")
            year = str[0].toInt()
        }
        logE("year----$year")
        return "${Calendar.getInstance().get(Calendar.YEAR) - year}岁"
    }

    fun getFamily(): String = familyName.ifEmpty { "无" }
}

@kotlinx.serialization.Serializable
data class PersonCar(
    val carIcon: String,
    val carId: String,
    val carName: String,
    val carSvga: String,
    val state: String? = "",
)


