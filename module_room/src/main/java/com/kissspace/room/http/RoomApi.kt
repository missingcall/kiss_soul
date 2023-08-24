package com.kissspace.room.http

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 14:43
 * @Description: 房间接口管理
 *
 */
object RoomApi {
    //用户上麦
    const val API_UP_MIC = "/djsoul-chatroom/microphone/getMicrophone"

    //用户下麦
    const val API_QUIT_MIC = "/djsoul-chatroom/microphone/leaveMicrophone"

    //创建房间
    const val API_CREATE_ROOM = "/djsoul-chatroom/chatRoom/createPersonChatRoom"

    //加入房间
    const val API_JOIN_ROOM = "/djsoul-chatroom/chatRoom/getIntoRoom"

    //刷新房间
    const val API_REFRESH_ROOM = "/djsoul-chatroom/chatRoom/refreshIntoRoom"

    //获取礼物tab列表
    const val API_GET_GIFT_TABS = "/djsoul-user/giftTab/queryAll"

    //根据tab id查询礼物列表
    const val API_GET_GIFT_BY_ID = "/djsoul-user/giftInfo/queryGiftByTabId"

    //送礼接口
    const val API_SEND_GIFT = "/djsoul-user/giftInfo/giveGiftForSomebody"

    //获取麦位列表用户
    const val API_GET_ON_MIC_USERS = "/djsoul-chatroom/microphone/getOnMicrophoneUser"

    //锁麦
    const val API_LOCK_MIC = "/djsoul-chatroom/microphone/lockMicrophone"

    //解锁麦
    const val API_UNLOCK_MIC = "/djsoul-chatroom/microphone/unlockMicrophone"

    //设置房间密码
    const val API_SET_PASSWORD = "/djsoul-chatroom/chatRoom/updatePassword"

    //获取房间排麦用户列表
    const val API_GET_MIC_QUEUE_LIST = "/djsoul-chatroom/microphone/queryMicrophoneWaitingQueue"

    //获取房间可修改信息
    const val API_GET_ROOM_INFO = "/djsoul-chatroom/chatRoom/queryChatRoomById"

    //修改房间信息
    const val API_UPDATE_ROOM_INFO = "/djsoul-chatroom/chatRoom/UpdateChatRoom"

    //抱上麦
    const val API_INVITE_MIC = "/djsoul-chatroom/microphone/inviteUserToMicrophone"

    //抱下麦
    const val API_KICK_MIC = "/djsoul-chatroom/microphone/kickOutUserFromMicrophone"

    //取消排麦
    const val API_CANCEL_QUEUE = "/djsoul-chatroom/microphone/cancelMicrophoneWaiting"

    //获取房间在线用户列表
    const val API_GET_ROOM_ONLINE_USER = "/djsoul-user/onlineUsers/queryOnlineUserList"

    //设置用户身份
    const val API_UPDATE_USER_ROLE = "/djsoul-chatroom/chatRoomRole/insertChatRoomRole"

    //取消用户管理员身份
    const val API_CANCEL_MANAGER = "/djsoul-chatroom/chatRoomRole/updateChatRoomRole"

    //关闭密码房
    const val API_CLOSE_PASSWORD = "/djsoul-chatroom/chatRoom/closePassword"

    //设置房间背景
    const val API_SETTING_ROOM_BACKGROUND = "/djsoul-chatroom/chatRoom/setBackground"

    //获取背包礼物列表
    const val API_GET_PACK_GIFT_LIST = "/djsoul-user/giftBag/queryGiftBagByUserId"

    //禁麦
    const val API_BAN_MIC = "/djsoul-chatroom/chatRoom/forbiddenMike"

    //取消禁麦
    const val API_CANCEL_BAB_MIC = "/djsoul-chatroom/chatRoom/cancelForbiddenMike"

    //禁言
    const val API_BAN_CHAT = "/djsoul-chatroom/chatRoom/chatRoomMuted"

    //获取用户资料卡信息
    const val API_GET_USER_PROFILE_INFO = "/djsoul-user/user/getUserCard"

    //获取预言列表
    const val API_GET_PREDICTION_LIST = "/djsoul-game/integralGuess/getIntegralGuessInRoom"

    //获取参加过的预言记录列表
    const val API_GET_PREDICTION_HISTORY = "/djsoul-game/integralGuess/getIntegralGuessBetHistory"

    //创建预言
    const val API_CREATE_PREDICTION = "/djsoul-game/integralGuess/createIntegralGuess"

    //中止投注
    const val API_STOP_PREDICTION = "/djsoul-game/integralGuess/stopBet"

    //结算竞猜
    const val API_SETTLE_PREDICTION = "/djsoul-game/integralGuess/settleGuess"

    //删除竞猜
    const val API_DELETE_PREDICTION = "/djsoul-game/integralGuess/deleteGuess"

    //下注
    const val API_PREDICTION_BET = "/djsoul-game/integralGuess/bet"

    //积分广场列表
    const val API_PREDICTION_SQUARE_LIST =
        "/djsoul-game/integralGuess/getIntegralGuessInProgressInSquare"

    //积分竞猜排行榜
    const val API_PREDICTION_RANKING = "/djsoul-game/integralGuess/getIntegralRanking"

    //查询用户是否收藏了房间
    const val API_IS_COLLECT_ROOM = "/djsoul-user/userCollect/queryCollectChatRoom"

    //收藏房间
    const val API_COLLECT_ROOM = "/djsoul-user/userCollect/collectChatRoom"

    //收藏歌曲
    const val API_COLLECT_MUSIC = "/djsoul-user/userCollect/collectMusic"

    //收藏歌曲列表
    const val API_COLLECT_MUSIC_LIST = "/djsoul-user/userCollect/queryUserCollectMusic"

    //获取房间任务列表
    const val API_GET_TASK_REWARD_LIST =
        "/djsoul-task/taskInfo/selectTaskInfoListForChatroomIntegral"

    //获取所有积分
    const val API_GET_ALL_INTEGRAL =
        "/djsoul-task/taskInfo/batchReceiveTaskReward"

    //房间排行榜
    const val API_ROOM_RANK_USER = "/djsoul-user/systemRank/queryChatRoomRanking"

    //清除麦位魅力值
    const val API_CLEAR_CHARM_VALUE = "/djsoul-chatroom/microphone/clearCharmValue"

    //开积分盲盒
    const val API_OPEN_INTEGRAL_BOX = "/djsoul-user/mysteryBox/openPointsMysteryBox"

    //根据id查询竞猜
    const val API_GET_PREDICTION_BY_ID = "/djsoul-game/integralGuess/getIntegralGuessById"

    //魅力值开关
    const val API_SWITCH_INCOME = "/djsoul-chatroom/chatRoom/onOffChatRoomCharm"

    //踢出房间
    const val API_KICK_OUT_USER = "/djsoul-chatroom/chatRoom/kickOutOfTheRoom"

    //开始OBS直播
    const val API_START_OBS_LIVESTREAM = "/djsoul-chatroom/chatRoom/startTheLiveBroadcast"

    //结束obs直播
    const val API_STOP_OBS_LIVESTREAM = "/djsoul-chatroom/chatRoom/endthelivebroadcast"

    //设置直播横竖屏
    const val API_UPDATE_SCREEN_DIRECTION = "/djsoul-chatroom/chatRoom/updateScreenStatus"

    //查询浇水奖池状态
    const val API_QUERY_WATER_GAME_POOL = "/djsoul-game/giftPoolConfig/queryUsingGiftPool"

    //退出房间
    const val API_EXIT_ROOM = "/djsoul-chatroom/chatRoom/exitChatRoom"

    //退出房间
    const val API_SHARE_CHAT_ROOM = "/djsoul-chatroom/chatRoom/shareChatRoom"

    //房间心跳
    const val API_ROOM_HEART_BEAT = "/djsoul-chatroom/chatRoom/heartbeat"

    //查询房间背景
    const val API_QUERY_ROOM_BACKGROUND = "/djsoul-chatroom/chatRoom/queryRoomBackgroundList"

    //房间流水
    const val API_ROOM_INCOME = "/djsoul-chatroom/chatRoom/getRoomFlowTotal"

    //房间流水-周榜
    const val API_ROOM_INCOME_WEEK = "/djsoul-chatroom/chatRoom/getRoomThisWeekFlowTotal"

    //发起pk
    const val API_START_PK = "/djsoul-chatroom/chatRoom/initiateRoomPKInitialization"

    //搜索
    const val API_SEARCH_CONTENT = "/djsoul-search/search/search/home"

    // 加入房间黑名单
    const val API_BAN_USER_IN_ROOM = "/djsoul-chatroom/chatRoom/setChatRoomBlackList"

    // 取消房间黑名单
    const val API_CANCEL_BAN_USER_IN_ROOM = "/djsoul-chatroom/chatRoom/cancelChatRoomBlackList"

    //房间黑名单列表
    const val API_ROOM_BLACK_LIST = "/djsoul-chatroom/chatRoom/chatRoomBlackList"


}
