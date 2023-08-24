package com.kissspace.common.model

import androidx.databinding.BaseObservable
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils

@kotlinx.serialization.Serializable
data class MyDressUpListBean(
    val commodityId: String,
    val commodityType: String,
    val expirationTime: Long,
    val userBagId: String,
    var wearState: String,
    val commodityName: String,
    val icon: String,
    var profilePath:String="",
    val svga: String,
    val leftDay: Long = TimeUtils.getTimeSpanByNow(expirationTime, TimeConstants.DAY)
) : BaseObservable() {

    fun getSurplusDay():String{
        val timeSpanByNowForDay = TimeUtils.getTimeSpanByNow(expirationTime, TimeConstants.DAY)
        if (timeSpanByNowForDay > 0){
            return "剩余${timeSpanByNowForDay}天"
        }else{
            val timeSpanByNowForSec = TimeUtils.getTimeSpanByNow(expirationTime, TimeConstants.SEC)
            return if (timeSpanByNowForSec > 0)  "剩余1天" else "已过期"
        }
    }
}