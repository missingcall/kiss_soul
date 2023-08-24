package com.kissspace.mine.http

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 21:13
 * @Description:
 *
 */
object MineApi {

    //分页查询家族列表
    const val API_GET_FAMILY_LIST_BY_PARAMETER =
        "/djsoul-family/family/selectFamilyInfoListByParameter"

    //分页查询家族列表 (在对应的家族里面)
    const val API_GET_FAMILY_LIST = "/djsoul-family/family/selectFamilyInfoList"

    //    //分页查询热门家族列表
    const val API_GET_FAMILY_HOT_LIST = "/djsoul-family/family/selectHotFamilyInfoList"

    //申请家族接口
    const val API_GET_FAMILY_USER_APPLY = "/djsoul-family/familyUserApply/userApply"

    //根据Id查询家族信息
    const val API_GET_FAMILY_BY_ID = "/djsoul-family/family/selectFamilyInfoById"

    //意见反馈列表
    const val API_QUERY_USER_FEEDBACK = "/djsoul-user/userFeedback/queryUserFeedback"

    //查询意见反馈类型列表
    const val API_QUERY_USER_FEEDBACK_TYPE = "/djsoul-user/userFeedbackType/queryUserFeedbackType"

    //添加用户意见反馈
    const val API_INSERT_USER_FEEDBACK = "/djsoul-user/userFeedback/insertUserFeedback"


    //用户已读信息之后修改成已读状态
    const val API_READ_STATUS_USER_FEEDBACK = "/djsoul-user/userFeedback/readStatusUserFeedback"


//    const val API_InsertUserFeedback = "/djsoul-user/userFeedback/insertUserFeedback"
//    const val API_InsertUserFeedback = "/djsoul-user/userFeedback/insertUserFeedback"
//    /djsoul-user/userFeedback/readStatusUserFeedback

    //查询家族信息
    const val API_GET_SELECT_FAMILY_INFO = "/djsoul-family/family/selectFamilyInfo"

    //根据参数获取家族流水列表
    const val API_GET_SELECT_QUERY_FAMILY_FLOWLIST = "/djsoul-family/flow/queryFamilyFlowList"

    //更新家族信息
    const val API_GET_UPDATE_FAMILY_INFO = "/djsoul-family/family/updateFamilyInfo"

    //移出家族
    const val API_GET_FAMILY_MOVE_OUT = "/djsoul-family/family/moveOut"

    //审核用户加入家族申请
    const val API_GET_FAMILY_CHECK_USER_APPLY = "/djsoul-family/familyUserApply/checkUserApply"

    //查询举报类型
    const val API_QueryInformantType = "/djsoul-user/informantType/queryInformantType"

    //举报用户
    const val API_REPORT_USER = "/djsoul-user/informantUser/insertInformantUser"

    //举报房间
    const val API_REPORT_CHATROOM = "/djsoul-chatroom/informantChatRoom/insertInformantChatRoom"


    //更新家族头像
    const val API_GET_FAMILY_UPLOAD_ICON = "/djsoul-family/family/uploadFamilyIcon"

    //查询所有申请加入家族用户信息
    const val API_GET_FamilyUserApplyList =
        "/djsoul-family/familyUserApply/selectFamilyUserApplyListByParameter"

    //查询所有家族成员信息
    const val API_GET_FAMILY_USER_LIST = "/djsoul-family/family/selectFamilyUserList"

    //根据家族ID查询所有家族成员信息
    const val API_GET_FAMILY_USER_LIST_BY_ID =
        "/djsoul-family/family/selectFamilyUserListByFamilyId"

    const val API_QUERY_FAMILY_USER_LIST =
        "/djsoul-family/family/pageQueryFamilyUserList"


    const val API_BATCH_TRANSFER_DIAMOND =
        "/djsoul-user/userNum/batchTransferDiamond"


    //商城列表
    const val API_STORE_GOODS_LIST = "/djsoul-user/commodity/queryCommodityList"

    //购买装扮
    const val API_BUY_DRESS_UP = "/djsoul-user/commodity/buyCommodity"

    //查询我的装扮
    const val API_QUERY_MY_DRESS_UP = "/djsoul-user/userBag/myBag"

    //佩戴装扮
    const val API_WEAR_DRESS_UP = "/djsoul-user/commodity/wearedHeadwearOrCar"

    //取消佩戴坐骑
    const val API_CANCEL_WEAR_DRESS_UP = "/djsoul-user/commodity/cancelWearedHeadwearOrCar"


    //取消收藏
    const val API_CANCEL_COLLECT = "/djsoul-user/userCollect/collectChatRoom"

    //我的关注
    const val API_QUERY_MY_FOLLOW = "/djsoul-user/userAttention/getUserAttentionList"

    //我的访客
    const val API_QUERY_MY_VISITOR = "/djsoul-user/visitor/queryUserVisitor"

    //我的粉丝
    const val API_QUERY_MY_FANS = "/djsoul-user/userAttention/getUserBeAttentionList"

    //查询用户主页信息
    const val API_QUERY_USER_PROFILE = "/djsoul-user/user/personalData"

    //获取礼物tab列表
    const val API_GET_GIFT_TABS = "/djsoul-user/giftTab/queryAll"

    //根据tab id查询礼物列表
    const val API_GET_GIFT_BY_ID = "/djsoul-user/giftInfo/queryGiftByTabId"

    //编辑个人资料
    const val API_EDIT_PROFILE = "/djsoul-user/user/editUserData"

    //任务中心
    const val API_TASK_CENTER = "/djsoul-task/taskInfo/selectTaskInfoList"

    //领取任务奖励
    const val API_RECEIVE_TASK_REWARD = "/djsoul-task/taskInfo/receiveTaskReward"


    //我的钱包
    const val API_MY_WALLET = "/djsoul-user/userNum/getMyMoneyBag"

    //收益兑换
    const val API_EXCHANGE_EARNS = "/djsoul-user/userNum/exchangeCoin"

    //钻石兑换
    const val API_EXCHANGE_DIAMOND = "/djsoul-user/userNum/exchangeDiamond"

    //提现
    const val API_WITHDRAW_ITEM = "/djsoul-payment/withdraw/withdrawDeposit"

    //金币转账
    const val API_TRANSFER_COIN = "/djsoul-user/userNum/transferCoin"

    //钻石转账
    const val API_TRANSFER_DIAMOND = "/djsoul-user/userNum/transferDiamond"


    //根据用户展示id获取通用用户信息
    const val API_QueryUserByDisplayIdResponse= "/djsoul-user/user/queryUserByDisplayIdResponse"


    //用户提现列表
    const val API_WithDrawList = "/djsoul-payment/withdraw/withdrawList"

    //分页获取金币流水记录列表
    const val API_FLOW_RECORD_COIN_LIST = "/djsoul-user/flowRecord/queryFlowRecordCoinPage"

    //分页获取收益列表
    const val API_FLOW_RECORD_EARNS_LIST = "/djsoul-user/flowRecord/queryFlowRecordProfitPage"

    //分页获取钻石流水记录列表
    const val API_FLOW_RECORD_DIAMOND_LIST = "/djsoul-user/flowRecord/queryFlowRecordDiamondPage"

    //获取用户魅力等级列表
    const val API_QUERY_USER_CHARM = "/djsoul-user/grade/queryUserCharm"

    //获取用户财富等级列表
    const val API_QUERY_USER_CONSUME = "/djsoul-user/grade/queryUserConsume"

    //绑定支付宝
    const val API_BIND_ALIPAY = "/djsoul-user/authentication/accountBinding"

    //获取我的页面里面新消息状态
    const val API_QUERY_MESSAGE_STATUS = "/djsoul-user/user/queryNewMessageStatus"

    //查询所有礼物
    const val API_QUERY_ALL_GIFT = "/djsoul-user/giftInfo/queryGiftInfoNotBox"

    //设置/取消家族管理员
    const val API_FAMILY_SETTING_MANAGER = "/djsoul-family/family/setFamilyAdmin"

}