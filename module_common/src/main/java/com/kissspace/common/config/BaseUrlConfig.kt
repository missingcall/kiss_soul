package com.kissspace.common.config

import com.kissspace.common.util.mmkv.MMKVProvider


/**
 * 当前是否是测试环境
 */
val isTestServer: Boolean
    get() = MMKVProvider.baseUrl == BaseUrlConfig.BASEURL_TEST

/**
 * 当前是否是预发环境
 */
val isPreServer: Boolean
    get() = MMKVProvider.baseUrl == BaseUrlConfig.BASEURL_PRE

/**
 * 当前是否是生产环境
 */
//val isReleaseServer: Boolean
////    get() = MMKVProvider.baseUrl == BaseUrlConfig.BASEURL_RELEASE

val isReleaseServer = false
/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/9 15:19
 * @Description: baseUrl 管理类
 *
 */

object BaseUrlConfig {
    /**
     * 测试环境
     */
    const val BASEURL_TEST = "http://121.41.169.40:8201"

    /**
     *  预发环境
     */
    const val BASEURL_PRE = "https://prodapi.qushengfun.com"

    /**
     * 生产环境
     */
    const val BASEURL_RELEASE = "https://prod.wzlsdzswyxgs.com"


    /**
     * 获取H5域名
     */
    fun getH5BaseUrl() = when {
        isReleaseServer -> "https://h5api.wzlsdzswyxgs.com"
        isPreServer -> "https://prodapi.wzlsdzswyxgs.com"
        isTestServer -> "https://test_water.qusheng.fun"
        else -> "https://h5api.qushengfun.com"
    }
}