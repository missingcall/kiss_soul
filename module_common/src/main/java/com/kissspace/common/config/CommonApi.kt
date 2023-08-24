package com.kissspace.common.config

/**
 * 公用请求api
 */
object CommonApi {
    //获取用户信息
    const val API_GET_USER_INFO = "/djsoul-user/user/info"

    //跟据id查询用户信息
    const val API_GET_USER_INFO_BY_ID = "/djsoul-user/user/queryUserByUserId"

    //获取app全局配置
    const val API_GET_APP_CONFIG = "/djsoul-user/djsConfigList/queryOneConfigByKey"

    //关注用户
    const val API_FOLLOW_USER = "/djsoul-user/userAttention/attentionUser"

    //取消关注用户
    const val API_CANCEL_FOLLOW_USER = "/djsoul-user/userAttention/cancelAttentionUser"

    //查询房间是否有密码
    const val API_QUERY_ROOM_PASSWORD = "/djsoul-chatroom/chatRoom/getChatRoomRoomPwd"

    //查询emoji表情列表
    const val API_QUERY_EMOJI_LIST = "/djsoul-user/chatEmoji/queryAllEmojiList"

    //拉黑用户
    const val API_BAN_USER = "/djsoul-user/user/userBlacklist"

    //取消拉黑
    const val API_CANCNEL_BAN_USER = "/djsoul-user/user/userCancelBlacklist"

    //查询用户主页信息
    const val API_QUERY_USER_PROFILE = "/djsoul-user/user/personalData"

    //用户充值列表
    const val API_SelectPayChannelList = "/djsoul-payment/pay/selectPayChannelList"

    //发送验证码
    const val API_SendSms = "/djsoul-user/sms/sendCode"

    //根据手机号获取用户账号信息列表
    val API_USER_LIST_BY_PHONE = "/djsoul-user/user/queryUserListByPhone"

    //创建账号
    val API_USER_ACCOUNT_CREATE = "/djsoul-user/user/accountCreate"

    //根据用户id登录
    val API_LOGIN_BY_USERID = "/djsoul-user/user/loginByUserId"

    //我的收藏
    const val API_QUERY_MY_COLLECT = "/djsoul-user/userCollect/queryUserCollect"

    //校验支付订单结果
    const val API_IPayNotify = "/djsoul-payment/notice/payNotify"

    //领取房间任务奖励
    const val API_RECEIVE_TASK_REWARD = "/djsoul-task/taskInfo/receiveTaskReward"

    //系统消息
    const val API_SYSTEM_MESSAGE = "/djsoul-user/systemMessage/pageQuery"

    //任务-发送消息
    const val API_TASK_SEND_MESSAGE = "/djsoul-chatroom/message/sendMessage"

    //首页潮播banner
    const val API_HOME_BANNER_CHAOBO = "/djsoul-chatroom/recommend/getTrendLiveStreaming"

    //首页派对banner
    const val API_HOME_BANNER_PARTY = "/djsoul-chatroom/recommend/getParty"

    const val API_GET_RANDOM_ROOM = "/djsoul-chatroom/chatRoom/getRandomRoom"

    //短信验证码校验
    const val API_VERIFICATION_CODE = "/djsoul-user/sms/verificationCode"

    //杉德支付
    const val API_SAND_PAY = "/djsoul-payment/pay/sandPay"

    const val QUERY_PLATFORM_RANKING = "/djsoul-user/systemRank/queryPlatformRanking"

}