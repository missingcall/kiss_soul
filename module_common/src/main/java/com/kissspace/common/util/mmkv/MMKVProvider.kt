package com.kissspace.common.util.mmkv

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import com.dylanc.mmkv.*
import com.netease.nimlib.sdk.auth.LoginInfo
import com.kissspace.common.config.BaseUrlConfig
import com.kissspace.common.config.Constants
import com.kissspace.common.model.LoginResultBean
import com.kissspace.common.model.UpgradeBean
import com.kissspace.common.model.config.RoomGameConfig
import com.kissspace.common.model.config.WaterConfig
import com.kissspace.util.isNotEmpty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/7
 * @Description: MMKV封装类 属性委托实现配置读写
 *
 */
object MMKVProvider : MMKVOwner {

    //baseUrl
    var baseUrl by mmkvString(default = BaseUrlConfig.BASEURL_TEST)

    //登录信息
    var loginResult by mmkvGson<LoginResultBean>()

    //用户id
    var userId by mmkvString(default = "")

    //用户性别
    var sex by mmkvString(default = Constants.SEX_FEMALE)

    //用户特权身份
    var privilege by mmkvString(default = "001")

    //用户头像
    var userAvatar by mmkvString(default = "")

    //手机号码
    var userPhone by mmkvString(default = "")

    //用户云信id
    var accId by mmkvString(default = "")

    //用户显示ID
    var displayId by mmkvString(default = "")

    //是否编辑过个人信息
    var isEditProfile by mmkvBool(true)

    //是否显示浇水信息
    var room_water_info_show by mmkvBool(default = true)

    //是否显示礼物特效
    var room_gift_effect_show by mmkvBool(default = true)

    //是否静音
    var room_mute by mmkvBool(default = false)

    //h5页面显示浇水动画
    var isShowWaterAnimation by mmkvBool(default = false)

    //上次显示升级弹窗的时间戳
    var lastShowUpgradeDate by mmkvLong(0)

    //上次打开礼物弹窗选择的tab栏
    var lastGiftTabIndex by mmkvInt(0)

    //上次打开礼物弹窗选择的礼物id
    var lastGiftIds by mmkvGson<String>()

    //上次是否选中了积分盲盒
    var isLastCheckIntegralBox by mmkvBool(false)

    //搜索历史记录
    var searchContent by mmkvString(default = "")


    //当前访客数量
    var currentVisitorCount by mmkvInt(default = 0)

    //当前粉丝数量
    var currentFansCount by mmkvInt(default = 0)

    //是否有首充
    var firstRecharge by mmkvBool(default = true)


    //协议是否同意
    var isAgreeProtocol by mmkvBool(default = false)

    //身份认证
    var authentication by mmkvBool(default = false)

    //数美的deviceId
    var sm_deviceId by mmkvString(default = "")

    //青少年是否设置过密码
    var adolescent by mmkvBool(default = false)

    //青少年显示的弹窗时间
    var adolescentShowTime by mmkvLong(default = 0L)

    //青少年弹窗用户
    var adolescentList by mmkvString(default = "")

    var fullName by mmkvString(default = "")

    var idNumber by mmkvString(default = "")

    //系统消息上次已读数量
    var systemMessageLastReadCount by mmkvInt(0)

    //系统消息未读数量
    var systemMessageUnReadCount by mmkvInt(0)

    //是否显示大佬榜
    var isShowBossRank by mmkvBool(false)

    //是否登录云信
    var isLoginNetEase by mmkvBool(false)

    //是否显示房间新手引导
    var isShowRoomGuide by mmkvBool(false)

    //微信公众号
    var wechatPublicAccount by mmkvString(default = "")

    //聊天财富等级最低限制
    var userChatMinLevel by mmkvInt(default = 2)

    //是否显示游戏
    var isShowGame by mmkvBool(default = false)

    var gameConfig by mmkvString()

    var userHour by mmkvInt(default = 0)
    var userHourDate by mmkvString(default = "")

}

/**
 * 是否显示更新弹窗
 * 1.强更每次打开都显示
 * 2.非强更每天显示一次
 */
fun UpgradeBean.isShowUpgrade(): Boolean {
    return if (this.isForcedUpdate == 1) true else !TimeUtils.isToday(MMKVProvider.lastShowUpgradeDate)
}

/**
 * 是否登录过
 */
fun isLogin(): Boolean {
    return MMKVProvider.loginResult != null && MMKVProvider.loginResult?.token != null && MMKVProvider.loginResult?.token.isNotEmpty() && MMKVProvider.isLoginNetEase
}


fun clearMMKV() {
    val isAgreeProtocol = MMKVProvider.isAgreeProtocol
    val baseUrl = MMKVProvider.baseUrl
    val isShowRoomGuide = MMKVProvider.isShowRoomGuide
    val adolescentList = MMKVProvider.adolescentList
    kv.clearMemoryCache()
    kv.clearAll()
    MMKVProvider.isAgreeProtocol = isAgreeProtocol
    MMKVProvider.baseUrl = baseUrl
    MMKVProvider.isShowRoomGuide = isShowRoomGuide
    MMKVProvider.adolescentList = adolescentList
}


inline fun <reified T : Any> mmkvGson() = MMKVGsonProperty(T::class.java)

class MMKVGsonProperty<V : Any>(
    private val clazz: Class<V>
) : ReadWriteProperty<MMKVOwner, V?> {
    override fun getValue(thisRef: MMKVOwner, property: KProperty<*>): V? =
        GsonUtils.fromJson(thisRef.kv.decodeString(property.name), clazz)

    override fun setValue(thisRef: MMKVOwner, property: KProperty<*>, value: V?) {
        thisRef.kv.encode(property.name, GsonUtils.toJson(value))
    }
}

