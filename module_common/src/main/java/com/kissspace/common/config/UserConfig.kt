package com.kissspace.common.config

import com.netease.nimlib.sdk.auth.LoginInfo
import com.kissspace.common.model.LoginResultBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/2/13 11:21.
 * @Describe 保存用户配置信息
 */
object UserConfig {
    fun saveUserConfig(userInfoBean: UserInfoBean, loginResult: LoginResultBean) {
        MMKVProvider.isEditProfile = loginResult.information
        MMKVProvider.isLoginNetEase = true
        saveUserConfig(userInfoBean)
    }

    fun saveUserConfig(userInfoBean: UserInfoBean) {
        MMKVProvider.userPhone = userInfoBean.mobile
        MMKVProvider.userId = userInfoBean.userId
        MMKVProvider.displayId = userInfoBean.displayId
        MMKVProvider.privilege = userInfoBean.privilege
        MMKVProvider.accId = userInfoBean.accId.orEmpty()
        MMKVProvider.isShowBossRank =
            userInfoBean.userRightList.contains(Constants.UserPermission.PERMISSION_CHECK_RICH_RANK)
        MMKVProvider.sex = userInfoBean.sex
        MMKVProvider.firstRecharge = userInfoBean.firstRecharge
        MMKVProvider.authentication = userInfoBean.authentication
        MMKVProvider.fullName = userInfoBean.fullName.orEmpty()
        MMKVProvider.idNumber = userInfoBean.idNumber.orEmpty()
        MMKVProvider.isShowGame = userInfoBean.consumeLevel >= 2
    }

}