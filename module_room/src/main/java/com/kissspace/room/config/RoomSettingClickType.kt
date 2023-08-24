package com.kissspace.room.config


sealed class RoomSettingClickType {
    //积分领取
    object TYPE_GET_INTEGRAL : RoomSettingClickType()

    //积分竞猜
    object TYPE_PREDICTION : RoomSettingClickType()

    //房间PK
    object TYPE_PK : RoomSettingClickType()

    //浇水信息
    object TYPE_WATER : RoomSettingClickType()

    //黑名单
    object TYPE_BLACKLIST : RoomSettingClickType()

    //礼物特效
    object TYPE_GIFT_EFFECT : RoomSettingClickType()

    //静音
    object TYPE_MUTE : RoomSettingClickType()

    //举报
    object TYPE_REPORT : RoomSettingClickType()

    //是否显示魅力值
    object TYPE_INCOME : RoomSettingClickType()

    //视频设置
    object TYPE_VIDEO_DIRECTION : RoomSettingClickType()

    //清除魅力值
    object TYPE_CLEAN_INCOME : RoomSettingClickType()

    //房间信息
    object TYPE_ROOM_INFO : RoomSettingClickType()

    //房间密码
    object TYPE_PASSWORD : RoomSettingClickType()

    //房间背景
    object TYPE_BACKGROUND : RoomSettingClickType()

    //背景音乐
    object TYPE_BACKGROUND_MUSIC : RoomSettingClickType()

    //设置管理员
    object TYPE_SETTING_MANAGER : RoomSettingClickType()

    //清除公屏信息
    object TYPE_CLEAR_SCREEN_MESSAGE : RoomSettingClickType()

    object TYPE_GAME1 : RoomSettingClickType()

    object TYPE_GAME2 : RoomSettingClickType()
    object TYPE_GAME3 : RoomSettingClickType()
    object TYPE_GAME : RoomSettingClickType()


}