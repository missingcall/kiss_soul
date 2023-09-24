package com.kissspace.dynamic.http

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 21:13
 * @Description:
 *
 */
object DynamicApi {

    //发布动态
    const val API_INSERT_DYNAMICS = "/djsoul-user/dynamics/insertDynamics"

    //获取动态列表
    const val API_GET_DYNAMICS_LIST = "/djsoul-user/dynamics/pageQueryDynamicsList"


    const val API_GET_DYNAMICS_LIST_RECOMMEND = "/djsoul-user/dynamics/pageQueryDynamicsList"

    const val API_GET_DYNAMICS_LIST_FOLLOW = "/djsoul-user/dynamics/queryDynamicsFollowWithInterestList"

    const val API_LIKE_DYNAMICS = "/djsoul-user/dynamics/likeOrCancelLiking"

    const val API_DYNAMIC_FOLLOW = "/djsoul-user/dynamics/likeOrCancelLiking"

    const val API_GET_COMMENT_DETAIL = "/djsoul-user/dynamics/obtainUserInformationForComments"

    const val API_GET_DYNAMIC_LILES = "/djsoul-user/dynamics/obtainUserInformationForLikes"

    const val API_ADD_COMMENT = "/djsoul-user/dynamics/insertCommentsDynamic"


}