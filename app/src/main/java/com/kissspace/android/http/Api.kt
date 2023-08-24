package com.kissspace.android.http

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/10
 * @Description: api地址
 *
 */
object Api {

    //获取房间列表
    const val API_GET_ROOM_LIST = "/djsoul-chatroom/chatRoom/queryChatRoomList"

    //检查版本
    const val API_UPGRADE = "/djsoul-user/configVersion/checkVersion"

    //打招呼
    const val API_SAY_HI = "/djsoul-user/userMessage/sayHello"

    //是否可以获取邀请码
    const val API_GET_BOOLEAN_IS_INVITATION = "/djsoul-user/invitationCodeReward/popup"

    //提交邀请码
    const val API_SUBMIT_INVITATION_CODE= "/djsoul-user/invitationCodeReward/submit"

    //搜索
    const val API_SEARCH_CONTENT = "/djsoul-search/search/search/home"

    //实名认证
    const val API_IDENTITY_AUTH_VIEW = "/djsoul-user/authentication/insertUserAuthentication"

    //活体认证
    const val API_FACE_RECOGNITION = "/djsoul-user/authentication/faceRecognition"

}