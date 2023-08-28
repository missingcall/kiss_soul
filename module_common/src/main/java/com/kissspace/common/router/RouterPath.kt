package com.kissspace.common.router

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/4
 * @Description: 路由地址
 *
 */
object RouterPath {
    private const val PATH_BASE = "/djs/android/"

    //MAIN ACT
    const val PATH_MAIN = "djs://pages/main"

    //语音直播房间
    const val PATH_LIVE_AUDIO = PATH_BASE + "live/audio"

    //潮播视频房间
    const val PATH_LIVE_VIDEO = PATH_BASE + "live/video"

    //游戏直播房间
    const val PATH_LIVE_GAME = PATH_BASE + "live/game"

    //登录
    const val PATH_LOGIN = PATH_BASE + "login"


    //验证码登录
    const val PATH_LOGIN_PHONE_CODE = PATH_BASE + "login/phone_code"

    //输入验证码
    const val PATH_INPUT_SMS_CODE = PATH_BASE + "login/input_sms_code"

    //选择账号
    const val PATH_CHOOSE_ACCOUNT = PATH_BASE + "user/choose_account"

    //完善个人信息
    const val PATH_LOGIN_EDIT_PROFILE = PATH_BASE + "login/edit/profile"

    //webview
    const val PATH_WEBVIEW = "djs://pages/browser"

    const val PATH_H5WebActivity = "djs://pages/h5web"


    const val PATH_SEARCH = PATH_BASE + "search"

    //参与预言页面
    const val PATH_ROOM_JOIN_PREDICTION = PATH_BASE + "room/join_prediction"

    //设置背景页面
    const val PATH_ROOM_SETTING_BACKGROUND = PATH_BASE + "room/setting_background"

    //聊天页面
    const val PATH_CHAT = PATH_BASE + "chat"

    //设置页面
    const val PATH_SETTING = "djs://pages/setting"

    //选择账号
    const val PATH_CHANGE_ACCOUNT = PATH_BASE + "user/change_account"

    //账号安全
    const val PATH_ACCOUNT = PATH_BASE + "setting/account"

    //发送验证码
    const val PATH_SEND_SMS_CODE = PATH_BASE + "setting/send_sms_code"

    //注销账号
    const val PATH_LOG_OFF_ACCOUNT = PATH_BASE + "setting/log_off_account"

    //消息提示设置
    const val PATH_MESSAGE_NOTIFY = PATH_BASE + "setting/message_notify"

    //打招呼设置
    const val PATH_SAY_HI_SETTING = PATH_BASE + "setting/say_hi"

    //黑名单
    const val PATH_BLACK_LIST = PATH_BASE + "setting/black_list"

    //帮助
    const val PATH_HELP = PATH_BASE + "setting/help"

    //关于我们
    const val PATH_ABOUT_US = PATH_BASE + "setting/about_us"


    //家族详情页面
    const val PATH_FAMILY_DETAIL = PATH_BASE + "family_detail"

    //家族列表页面
    const val PATH_FAMILY_LIST = PATH_BASE + "family_list"

    //家族列表页面
    const val PATH_FAMILY_MEMBER_LIST = PATH_BASE + "family_member_list"

    //查询所有申请家族用户的列表
    const val PATH_FAMILY_APPLY_LIST = "djs://pages/family/family_apply_list"

    //家族搜索页面
    const val PATH_SEARCH_FAMILY = PATH_BASE + "search_family"

    //修改家族资料页面
    const val PATH_MODIFY_FAMILY = PATH_BASE + "family_modify"

    //家族收益页面
    const val PATH_FAMILY_PROFIT = PATH_BASE + "family_profit"

    //家族对外收益页面
    const val PATH_FAMILY_OUT_PROFIT = PATH_BASE + "family_out_profit"

    //装扮商城
    const val PATH_STORE = "djs://pages/store"

    //用户钱包首页
    const val PATH_USER_WALLET = PATH_BASE + "user_wallet"

    //金币充值页面
    const val PATH_USER_WALLET_GOLD_RECHARGE = "djs://pages/user_wallet_gold_recharge"

    //个人主页
    const val PATH_USER_PROFILE = PATH_BASE + "user_profile"

    //编辑资料页面
    const val PATH_EDIT_PROFILE = "djs://pages/mine_edit_profile"

    //编辑昵称页面
    const val PATH_EDIT_NICKNAME = PATH_BASE + "mine_edit_nickname"

    //编辑个性签名
    const val PATH_EDIT_SIGN = PATH_BASE + "mine_edit_sign"

    //金币钻石详情页面
    const val PATH_USER_WALLET_COIN_DIAMOND_DETAIL = PATH_BASE + "user_wallet_coin_diamond_detail"


    //用户钱包操作成功页面
    const val PATH_USER_WALLET_OPERATE_SUCCESS = PATH_BASE + "user_wallet_operate_success"

    //用户钱包转账页面
    const val PATH_USER_WALLET_TRANSFER = PATH_BASE + "user_wallet_transfer"

    //家族转账列表页面
    const val PATH_USER_WALLET_FAMILY_MEMBER_LIST =
        PATH_BASE + "user_wallet_wallet_family_member_list"

    //家族转账列表页面
    const val PATH_USER_WALLET_UPDATE_FAMILY_MEMBER_LIST =
        PATH_BASE + "user_wallet_wallet_update_family_member_list"

    //钻石兑换明细页面
    const val PATH_USER_WALLET_DIAMOND_EXCHANGE_DETAIL =
        PATH_BASE + "user_wallet_diamond_exchange_detail"

    //钻石转账明细
    const val PATH_USER_WALLET_DIAMOND_TRANSFER_DETAIL =
        PATH_BASE + "user_wallet_diamond_transfer_detail"

    //兑换页面
    const val PATH_USER_WALLET_EXCHANGE = PATH_BASE + "user_wallet_exchange"

    //提现页面
    const val PATH_USER_WALLET_WITHDRAW = PATH_BASE + "user_wallet_withDraw"

    //提现记录
    const val PATH_USER_WITHDRAW_RECODE = PATH_BASE + "user_wallet_withdraw_recode"

    //用户房间举报页面
    const val PATH_REPORT = PATH_BASE + "user_report"

    //实名认证
    const val PATH_USER_IDENTITY_AUTH = "djs://pages/user_identity_auth"

    //实名认证成功
    const val PATH_USER_IDENTITY_AUTH_SUCCESS = "djs://pages/user_identity_auth_success"

    //活体验证页面
    const val PATH_USER_BIOMETRIC = "djs://pages/user_biometric"

    //我的收藏
    const val PATH_MY_COLLECT = PATH_BASE + "mine_collect"

    //我的装扮
    const val PATH_MY_DRESS_UP = PATH_BASE + "mine_dress_up"

    //真爱墙

    const val PATH_LOVE_WALL = PATH_BASE + "love_wall"

    //关注列表
    const val PATH_MY_FOLLOW = PATH_BASE + "mine_follow"

    //粉丝列表
    const val PATH_MY_FANS = PATH_BASE + "mine_fans"

    //访客列表
    const val PATH_MY_VISITOR = PATH_BASE + "mine_visitor"

    //意见反馈类型页面
    const val PATH_FEEDBACK_TYPE_LIST = "djs://pages/feedback_type_list"

    //意见反馈记录页面
    const val PATH_FEEDBACK_RECODE_LIST = PATH_BASE + "feedback_recode_list"

    //任务中心
    const val PATH_TASK_CENTER_LIST = PATH_BASE + "mine_task_center_list"

    //添加意见反馈记录
    const val PATH_ADD_FEEDBACK = PATH_BASE + "feedback_add"

    //我的等级
    const val PATH_MY_LEVEL = PATH_BASE + "mine_level"


    const val PATH_TEENAGER_DESCRIBE = PATH_BASE + "mine_teenager_describe"

    //青少年模式
    const val PATH_TEENAGER_MODE = PATH_BASE + "mine_teenager_mode"

    //绑定支付宝
    const val PATH_BIND_ALIPAY = "djs://pages/mine_bind_alipay"

    //绑定银行卡
    const val PATH_BIND_BANKCARD = "djs://pages/mine_bind_bankcard"

    //系统消息
    const val PATH_SYSTEM_MESSAGE = PATH_BASE + "system_message"

    //base url切换页面
    const val PATH_BASE_URL_SETTING = "base_url"

    const val PATH_H5_SCHEMA = "djs://pages/h5schema"

    const val PATH_CASHIER_PAY = PATH_BASE + "path_cashier_pay"
    const val PATH_CASHIER_PAY1 = PATH_BASE + "path_cashier_pay1"

    const val PATH_ROOM_GAME = PATH_BASE + "room_game"

    //设置登录密码
    const val PATH_SETTING_LOGIN_PASSWORD = PATH_BASE + "setting_login_password"

    //修改密码
    const val PATH_UPDATE_LOGIN_PASSWORD = PATH_BASE + "update_login_password"

    //密码登录
    const val PATH_LOGIN_PASSWORD = PATH_BASE + "login_password"

    //忘记密码
    const val PATH_FORGET_PASSWORD = PATH_BASE + "forget_password"

    //官方认证
    const val PATH_MINE_AUTH = PATH_BASE + "mine_auth"


    //发布动态
    const val PATH_DYNAMIC_SEND_FRIEND = "djs://pages/dynamic_send_friend"



    //任务消息
    const val PATH_MESSAGE_TASK = PATH_BASE + "message_task"
}