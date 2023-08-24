package com.kissspace.common.config

/**
 * app统一配置接口请求KEY
 */
object AppConfigKey {
    //浇水游戏url
    const val KEY_WATER_GAME_URL = "water_url"

    //派对标签列表
    const val KEY_PARTY_TAG_LIST = "party_tag_list"

    //潮播标签列表
    const val KEY_CHAO_BO_TAG_LIST = "chao_bo_tag_list"

    //房间背景图片列表
    const val KEY_CHAT_ROOM_BACKGROUND_URL_LIST = "chat_room_background_url_list"

    //默认头像列表
    const val KEY_DEFAULT_AVATAR = "default_avatar"

    //潮播banner
    const val KEY_HOME_BANNER = "chao_bo_banner_list"

    //派对banner
    const val KEY_PARTY_BANNER = "party_banner_list"

    //真爱强最新榜
    const val KEY_LOVE_WALL_RULE_NEWEST = "gift_wall_latest_rank_threshold"

    //真爱强最壕榜
    const val KEY_LOVE_WALL_RULE_RICHEST = "gift_wall_richest_rank_threshold"

    //派对banner，插在房间列表里的
    const val KEY_PARTY_BANNER_ROOM_LIST = "party_activities_banner_list"

    //派对banner，插在房间列表里的
    const val KEY_CHAOBO_BANNER_ROOM_LIST = "chao_bo_activities_banner_list"

    //积分盲盒所属的tabId
    const val KEY_POINTS_BOX_TAB_ID = "points_box_tab_id"

    //幸运礼物tabId
    const val KEY_GIFT_LUCKY_TAB_ID = "gift_tab_lucky_id"

    //派对房间banner
    const val KEY_ROOM_PARTY_ACTIVITY = "partyroom_activities_list"

    //潮播房间banner
    const val KEY_ROOM_CHAOBO_ACTIVITY = "chao_boroom_activities_list"

    //家族qq
    const val FAMILY_QQ_NUMBER = "family_qq_number"

    //运营微信
    const val COMMERCE_WECHAT = "commerce_wechat"

    //分享的url
    const val SHARE_WECHAT_URL = "wechat_share_url"
//    //分享的描述
//    const val SHARE_WECHAT_DESC = "wechat_share_desc"

    //系统守护房间id
    const val DAEMON_NETEASE_ROOM_ID = "daemon_netease_room_id"

    //浇水配置
    const val WATER_MODEL_SWITCH_CONFIG = "model_switch_config"

    //卡牌配置
    const val CARD_MODEL_SWITCH_CONFIG = "model_switch_config"

    //游戏icon
    const val ROOM_ACTIVE_ICON_CONFIG = "room_gameicon"

    //游戏配置
    const val ROOM_ACTIVE_PATH_CONFIG = "room_games_config"

    //允许聊天的最低财富等级
    const val CHAT_MIN_LEVEL = "chat_min_level"
}