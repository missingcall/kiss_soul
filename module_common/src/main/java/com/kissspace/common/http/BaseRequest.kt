package com.kissspace.common.http

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.scopeNetLife
import com.blankj.utilcode.util.GsonUtils
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.scope.AndroidScope
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.google.gson.reflect.TypeToken
import com.netease.htprotect.HTProtect
import com.kissspace.common.http.error.handleNetException
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.config.UserConfig
import com.kissspace.common.model.CheckRoomInfoModel
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.common.util.countDown
import com.kissspace.common.util.customToast
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.network.exception.AppException
import com.kissspace.network.exception.Error
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.util.fromJson
import com.kissspace.util.logE
import com.kissspace.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder

/**
 * 获取App通用配置
 */
inline fun <reified T> ViewModel.getAppConfigByKey(
    key: String, state: MutableSharedFlow<ResultState<T>>
) {
    scopeNetLife {
        val result = Get<String>(CommonApi.API_GET_APP_CONFIG) {
            param("configKey", key)
        }.await()
        val newResult = withContext(Dispatchers.IO) {
            URLDecoder.decode(result, "utf-8")
        }
        kotlin.runCatching {
            val type = object : TypeToken<T>() {}.type
            GsonUtils.fromJson<T>(newResult, type)
        }.onSuccess {
            state.emit(ResultState.onAppSuccess(it))
        }.onFailure {
            state.emit(ResultState.onAppError(AppException(Error.PARSE_ERROR, it)))
        }
    }.catch {
        launch {
            handleNetException(it, state)
        }
    }
}

inline fun <reified T> ViewModel.getAppConfigByKey(
    key: String, crossinline block: (T) -> Unit
) {
    scopeNetLife {
        val result = Get<String>(CommonApi.API_GET_APP_CONFIG) {
            param("configKey", key)
        }.await()
        val newResult = withContext(Dispatchers.IO) {
            URLDecoder.decode(result, "utf-8")
        }
        val type = object : TypeToken<T>() {}.type
        try {
            block(GsonUtils.fromJson(newResult, type))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            block(newResult as T)
        }
    }
}


inline fun <reified T> LifecycleOwner.getAppConfigByKey(
    key: String, crossinline block: (T) -> Unit
) {
    scopeNetLife {
        val result = Get<String>(CommonApi.API_GET_APP_CONFIG) {
            param("configKey", key)
        }.await()
        val newResult = withContext(Dispatchers.IO) {
            URLDecoder.decode(result, "utf-8")
        }
        val type = object : TypeToken<T>() {}.type
        try {
            block(GsonUtils.fromJson(newResult, type))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            block(newResult as T)
        }
    }
}


/**
 * 获取用户信息通用接口
 */
inline fun LifecycleOwner.getUserInfo(
    crossinline onSuccess: (UserInfoBean) -> Unit, crossinline onError: ((Throwable) -> Unit) = {}
) {
    scopeNetLife {
        val result = Get<UserInfoBean>(CommonApi.API_GET_USER_INFO).await()
        UserConfig.saveUserConfig(result)
        onSuccess(result)
    }.catch {
        onError(it)
    }
}

/**
 * 获取用户信息通用接口
 */
inline fun ViewModel.getUserInfo(
    crossinline onSuccess: (UserInfoBean) -> Unit = {},
    crossinline onError: ((Throwable) -> Unit) = {}
) {
    scopeNetLife {
        val result = Get<UserInfoBean>(CommonApi.API_GET_USER_INFO).await()
        UserConfig.saveUserConfig(result)
        onSuccess(result)
    }.catch {
        onError(it)
    }
}

/**
 * 获取用户信息通用接口
 */
inline fun getUserInfo(
    crossinline onSuccess: (UserInfoBean) -> Unit, crossinline onError: ((Throwable) -> Unit) = {}
) {
    scopeNet {
        val result = Get<UserInfoBean>(CommonApi.API_GET_USER_INFO).await()
        MMKVProvider.userId = result.userId
        MMKVProvider.privilege = result.privilege.orEmpty()
        MMKVProvider.sex = result.sex
        MMKVProvider.isShowBossRank =
            result.userRightList.contains(Constants.UserPermission.PERMISSION_CHECK_RICH_RANK)
        onSuccess(result)
    }.catch {
        onError(it)
    }
}


fun LifecycleOwner.isHasRoomPasswordId(
    crId: String,
    onSuccess: (CheckRoomInfoModel) -> Unit,
    onError: (String?) -> Unit
) {
    val params = mutableMapOf<String, Any?>("chatRoomId" to crId)
    request<CheckRoomInfoModel>(CommonApi.API_QUERY_ROOM_PASSWORD, Method.GET, params, onSuccess = {
        onSuccess.invoke(it)
    }, onError = {
        onError.invoke(it.message)
    })

}

/**
 *  检查用户权限
 *  @param permission 具体权限
 *
 */
fun LifecycleOwner.checkUserPermission(
    @Constants.UserPermissionType permission: String, block: (Boolean) -> Unit
) {
    getUserInfo(onSuccess = {
        if (it.userRightList.isEmpty()) {
            block(false)
        } else {
            block(it.userRightList.contains(permission))
        }
    })
}



//领取任务奖励
fun LifecycleOwner.getReceiveTaskReward(taskId: String, block: (Boolean) -> Unit) {
    val param = mutableMapOf<String, Any?>()
    param["taskId"] = taskId
    request<Boolean>(
        CommonApi.API_RECEIVE_TASK_REWARD, Method.POST, param, onSuccess = {
            block.invoke(it)
        },
        onError = {
            customToast(it.message)
        }
    )
}

fun LifecycleOwner.getWechatShareUrl(
    block: ((String) -> Unit)
) {
    getAppConfigByKey<String>(AppConfigKey.SHARE_WECHAT_URL) { url ->
        block.invoke(url)
    }
}

fun LifecycleOwner.uploadAudio(
    param: File, onSuccess: ((String) -> Unit) = {}, onError: (AppException) -> Unit = {}
): AndroidScope {
    return scopeNetLife {
        val result = Post<String>("/djsoul-user/oss/file/uploadAudio") {
            param("audio", param)
        }.await()
        onSuccess(result)
    }.catch {
        customToast("上传失败${it.message}")
        onError(handleNetException(it))
    }
}

fun LifecycleOwner.uploadPicture(
    param: MutableList<File>,
    onSuccess: ((MutableList<String>) -> Unit) = {},
): AndroidScope {
    return scopeNetLife {
        val result = Post<MutableList<String>>("/djsoul-user/oss/file/batchUploadImage") {
            for (file in param) {
                param("images", file)
            }
        }.await()
        onSuccess(result)
    }.catch {
        toast("图片上传失败${it.message}")
    }
}

fun LifecycleOwner.getSelectPayChannelList(
    onSuccess: ((List<WalletRechargeList>) -> Unit) = {},
): AndroidScope {
    val param = mutableMapOf<String, Any?>()
    param["os"] = Constants.OperationSystem
    return request<List<WalletRechargeList>>(CommonApi.API_SelectPayChannelList,
        Method.GET,
        param,
        onSuccess = {
            onSuccess.invoke(it)
        }, onError = {
            customToast(it.errorMsg)
        })
}

fun LifecycleOwner.requestPayServer(
    orderNo: String, onSuccess: (Boolean) -> Unit, onError: ((String?) -> Unit)? = null
): AndroidScope {
    val param = mutableMapOf<String, Any?>()
    param["orderNo"] = orderNo
    return request<Boolean>(url = CommonApi.API_IPayNotify, Method.POST, param, onSuccess = {
        onSuccess.invoke(it)
    }, onError = {
        onError?.invoke(it.message)
        //5秒 6次
        logE("--requestPayServer--" + it.errorMsg)
    })
}

fun LifecycleOwner.getPayResult(
    orderNo: String, onSuccess: () -> Unit, onError: ((String?) -> Unit)? = null
) {
    var mCountDown: Job? = null
    //隔5秒钟显示一次 30秒时间,倒计时
    mCountDown = countDown(
        6,
        step = 5000,
        reverse = false,
        scope = lifecycleScope,
        onTick = { tickTime ->
            requestPayServer(orderNo, onSuccess = {
                if (tickTime == 0L) {
                    toast("支付失败")
                    mCountDown?.cancel()
                } else {
                    if (it) {
                        toast("支付成功")
                        onSuccess.invoke()
                        mCountDown?.cancel()
                    }
                }
            }, onError = {
                onError?.invoke(it)
            })
        })
}

fun LifecycleOwner.verificationCode(
    phone: String,
    code: String,
    type: String,
    onError: (() -> Unit)? = null,
    onSuccess: ((Boolean) -> Unit)
): AndroidScope {
    val param = mutableMapOf<String, Any?>().apply {
        //类型：2-登录 3-注册 4-注销 5-找回密码 6-绑定手机号码 7-设置二级密码 8-绑定银行卡 9-找回账号 11-验证旧手机 12-快捷认证 13-人工认证 14-转账认证 15-绑定支付宝
        put("mobile", phone)
        put("code", code)
        put("type", type)
    }
    return request<Boolean>(url = CommonApi.API_VERIFICATION_CODE, Method.GET, param, onSuccess = {
        onSuccess.invoke(it)
    }, onError = {
        customToast(it.errorMsg)
        onError?.invoke()
    })
}

fun LifecycleOwner.sendSms(
    phone: String,
    type: String,
    onSuccess: ((String) -> Unit) = {},
): AndroidScope {
    val param = mutableMapOf<String, Any?>()
    //类型：2-登录 3-注册 4-注销 5-找回密码 6-绑定手机号码 7-设置二级密码 8-绑定银行卡 9-找回账号 11-验证旧手机 12-快捷认证 13-人工认证 14-转账认证
    param["mobile"] = phone
    param["type"] = type
    return request<String>(url = CommonApi.API_SendSms, Method.GET, param, onSuccess = {
        onSuccess.invoke(it)
    }, onError = {
        customToast(it.errorMsg)
    })
}

//创建支付订单
fun LifecycleOwner.createPayOrder(
    payProductId: String,
    payChannelType: String,
    onSuccess: ((String) -> Unit)?,
): AndroidScope {
    val param = mutableMapOf<String, Any?>()
    param["os"] = Constants.OperationSystem
    param["payProductId"] = payProductId
    param["payChannelType"] = payChannelType
    setApplicationValue(
        type = TypeFaceRecognition,
        value = Constants.FaceRecognitionType.CHARGE.type.toString()
    )
    return request<String>(url = "/djsoul-payment/pay/createPayOrder",
        Method.POST,
        param,
        onSuccess = {
            onSuccess?.invoke(it)
        },
        onError = {
            toast(it.errorMsg)
        })
}

fun ViewModel.uploadSingleFile(
    url: String,
    name: String,
    file: File?,
    onSuccess: ((String) -> Unit) = {},
): AndroidScope {
    return scopeNetLife {
        val result = Post<String>(url) {
            param(name, file)
        }.await()
        onSuccess(result)
    }.catch {
        customToast("图片上传失败${it.message}")
    }
}

fun ViewModel.uploadPicture(
    url: String,
    name: String,
    param: MutableList<File>,
    onSuccess: ((List<String>) -> Unit) = {},
): AndroidScope {
    return scopeNetLife {
        val result = Post<List<String>>(url) {
            for (file in param) {
                param(name, file)
            }
        }.await()
        onSuccess(result)
    }.catch {
        customToast("图片上传失败${it.message}")
    }
}

suspend fun ViewModel.getHtprotectTokenAsync(): String {
    val antiCheatResult = HTProtect.getToken(3000, ConstantsKey.NETEASE_RISK_MANAGEMENT_BUSINESSID)
    if (antiCheatResult.code == 200) {
        return antiCheatResult.token
    }
    return ""
}
