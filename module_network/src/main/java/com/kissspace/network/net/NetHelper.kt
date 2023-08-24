package com.kissspace.network.net

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeLife
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.drake.net.NetConfig
import com.drake.net.Post
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.okhttp.toNetOkhttp
import com.drake.net.scope.AndroidScope
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.kissspace.network.exception.AppException
import com.kissspace.network.exception.ExceptionHandle
import com.kissspace.network.result.ResultState
import com.kissspace.util.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File

sealed class Method {
    object GET : Method()
    object POST : Method()
}

fun netInit(
    host: String, context: Context? = null, config: OkHttpClient.Builder.() -> Unit = {}
) {
    NetConfig.host = host
    context?.let { NetConfig.app = it }
    val builder = OkHttpClient.Builder()
    builder.config()
    NetConfig.okHttpClient = builder.toNetOkhttp().build()
    NetConfig.converter = SerializationConverter()
}

/**
 *  activity/fragment里发起请求
 *
 *  @param url 请求连接
 *  @param method 请求方式
 *  @param param 请求参数
 *  @param isShowLoading 是否显示弹窗
 *  @param loadingMessage 弹窗信息
 *  @param state 请求结果包装
 */
inline fun <reified T> LifecycleOwner.request(
    url: String,
    method: Method,
    param: MutableMap<String, Any?> = mutableMapOf(),
    crossinline onSuccess: ((T) -> Unit) = {},
    crossinline onError: ((AppException) -> Unit) = {}
): AndroidScope {
    return scopeNetLife {
        val result = doRequest<T>(url, method, param)
        onSuccess(result)
    }.catch {
        onError(ExceptionHandle.parseException(it))
        handleError(it)
    }
}

/**
 *  viewModel里发起请求
 *
 *  @param url 请求连接
 *  @param method 请求方式的c
 *  @param param 请求参数
 *  @param isShowLoading 是否显示弹窗
 *  @param loadingMessage 弹窗信息
 *  @param state 请求结果包装
 */
inline fun <reified T> ViewModel.request(
    url: String,
    method: Method = Method.POST,
    param: MutableMap<String, Any?>? = mutableMapOf(),
    crossinline onSuccess: ((T) -> Unit) = {},
    crossinline onError: ((AppException) -> Unit) = {}
): AndroidScope {
    return scopeNetLife {
        val result = doRequest<T>(url, method, param)
        onSuccess(result)
    }.catch {
        onError(ExceptionHandle.parseException(it))
        handleError(it)
    }
}


/**
 *  viewModel里发起请求，自动将结果包装成SharedFlow emit出去
 *
 *  @param url 请求连接
 *  @param method 请求方式
 *  @param param 请求参数
 *  @param isShowLoading 是否显示弹窗
 *  @param loadingMessage 弹窗信息
 *  @param state 请求结果包装
 */
inline fun <reified T> ViewModel.request(
    url: String,
    method: Method = Method.POST,
    param: MutableMap<String, Any?>? = mutableMapOf(),
    state: MutableSharedFlow<ResultState<T>>
) {
    scopeNetLife {
        val result = doRequest<T>(url, method, param)
        state.emit(ResultState.onAppSuccess(result))
    }.catch { error ->
        scopeLife {
            state.emit(ResultState.onAppError(ExceptionHandle.parseException(error)))
        }
        handleError(error)
    }
}


inline fun <reified T> request(
    url: String,
    method: Method,
    param: MutableMap<String, Any?> = mutableMapOf(),
    crossinline onSuccess: ((T) -> Unit) = {},
    crossinline onError: ((AppException) -> Unit) = {}
): AndroidScope {
    return scopeNet {
        val result = doRequest<T>(url, method, param)
        onSuccess(result)
    }.catch {
        onError(ExceptionHandle.parseException(it))
        handleError(it)
    }
}

inline fun <reified T> CoroutineScope.asyncRequest(
    url: String,
    method: Method,
    params: MutableMap<String, Any?> = mutableMapOf()
): Deferred<T> {
    return if (method == Method.GET) {
        Get(url) {
            params?.let {
                for ((key, value) in params) {
                    when (value) {
                        is Number -> param(key, value as Number)
                        is String -> param(key, value as String)
                        is Boolean -> param(key, value as Boolean)
                    }
                }
            }
        }
    } else {
        Post(url) {
            val jsonParam = JSONObject()
            params?.let {
                for ((key, value) in params) {
                    value?.let {
                        jsonParam.put(key, value)
                    }
                }
            }
            json(jsonParam)
        }
    }
}

/**
 * 执行请求的具体操作
 * @param url 请求地址
 * @param method 请求方式
 * @param params 请求参数
 */
suspend inline fun <reified T> doRequest(
    url: String,
    method: Method,
    params: MutableMap<String, Any?>? = mutableMapOf(),
): T {
    return coroutineScope {
        when (method) {
            Method.POST -> {
                this.Post<T>(url) {
                    val jsonParam = JSONObject()
                    params?.let {
                        for ((key, value) in params) {
                            value?.let {
                                jsonParam.put(key, value)
                            }
                        }
                    }
                    json(jsonParam)

                }.await()
            }

            Method.GET -> {
                Get<T>(url) {
                    params?.let {
                        for ((key, value) in params) {
                            when (value) {
                                is Number -> param(key, value)
                                is String -> param(key, value)
                                is Boolean -> param(key, value)
                            }
                        }
                    }
                }.await()
            }
        }
    }
}

fun ViewModel.uploadFile(
    url: String,
    name: String,
    param: MutableList<File>,
    onSuccess: ((List<String>) -> Unit) = {},
    onError: (AppException) -> Unit = {}
): AndroidScope {
    return scopeNetLife {
        val result = Post<List<String>>(url) {
            for (file in param) {
                param(name, file)
            }
        }.await()
        onSuccess(result)
    }.catch { error ->
        scopeLife {
            onError(ExceptionHandle.parseException(error))
        }
    }
}

/**
 * 下载文件
 * @param url 下载地址
 * @param progress 下载进度回调
 * @param onSuccess 下载成功回调
 * @param onError 下载失败回调
 */
fun LifecycleOwner.downloadFile(
    url: String,
    progress: (Progress) -> Unit,
    onSuccess: (File) -> Unit,
    onError: (AppException) -> Unit
) {
    scopeNetLife {
        val file = Get<File>(url) {
            setDownloadDir(application.cacheDir)
            setDownloadFileNameDecode(true)
            addDownloadListener(object : ProgressListener() {
                override fun onProgress(p: Progress) {
                    progress(p)
                }
            })
        }.await()
        onSuccess(file)
    }.catch {
        onError(ExceptionHandle.parseException(it))
    }
}
