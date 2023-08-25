package com.kissspace.common.model

import android.os.Parcelable
import com.kissspace.common.config.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13
 * @Description: 用户信息返回实体
 *
 */
@Parcelize
@Serializable
data class UserInfoBean(
    val bgPath: List<String> = mutableListOf(),
    val birthday: String = "1994-03-23",
    val editDate: String = "",
    val info: String = "这个人很懒，什么都没留下",
    val isDestroy: Int,
    val privilege: String = "",
    val isFrozen: Int,
    val location: String? = "",
    val machineCode: String,
    val mobile: String,
    var nickname: String,
    var profilePath: String,
    val registerDate: String,
    var sex: String = Constants.SEX_MALE,
    val userId: String,
    val accId: String? = "",
    val displayId: String,
    val beautifulId: String = "",
    val coin: Double = 0.0,
    val integral: Double = 0.0,
    val followNum: Int = 0,
    val fansNum: Int = 0,
    val family: Boolean = false,
    val headOfFamily: Boolean = false,
    val collectionNum: Int = 0,
    val visitorNum: Int = 0,
    val charmLevel: Int = 0,
    val consumeLevel: Int = 0,
    val userRightList: List<String> = emptyList(),
    val firstRecharge: Boolean = false,
    //是否实名认证
    val authentication: Boolean = false,
    //魅力等级
    val consumeTotal: Double = 0.0,
    //财富等级
    val charmTotal: Double = 0.0,
    //青少年模式
    val adolescent: Boolean = false,
    //实际姓名
    val fullName: String? = null,
    //身份证号码
    val idNumber: String? = null,
    //银行卡号
    val cardNo: String = "",
    //坐骑
    val userCarUrl: String = "",
    //头像框
    val headwearUrl: String = "",
    val medalList: MutableList<UserMedalBean> = mutableListOf(),
    val isSetPassword: Boolean = false,
    val likesReceivedNum:String ="0",

) : Parcelable {
    fun getIntegralLong(): Long = integral.roundToLong()
}