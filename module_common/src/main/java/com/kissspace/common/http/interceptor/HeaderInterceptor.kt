package com.kissspace.common.http.interceptor

import android.util.Base64
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.bytedance.hume.readapk.HumeSDK
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.tencent.vasdolly.helper.ChannelReaderUtil
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.application
import com.kissspace.util.encryptMD5
import com.kissspace.util.ifNullOrEmpty
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/7
 * @Description: 处理header，添加公共参数
 *
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        addCheckSum(request, requestBuilder)
        val loginResult = MMKVProvider.loginResult
        requestBuilder.addHeader("Authorization", loginResult?.tokenHead + loginResult?.token)
        requestBuilder.addHeader("channel", appendChannel())
        //添加其他公共参数
        appendHeader(requestBuilder)
        return chain.proceed(requestBuilder.build())
    }

    private fun appendHeader(requestBuilder: Request.Builder) {
        requestBuilder.addHeader("deviceId", DeviceUtils.getUniqueDeviceId())
        requestBuilder.addHeader("packageName", AppUtils.getAppPackageName())
        requestBuilder.addHeader("versionNo", AppUtils.getAppVersionName())
        requestBuilder.addHeader(
            "equipmentName",
            android.os.Build.MANUFACTURER + (android.os.Build.MODEL)
        )
        requestBuilder.addHeader("equipmentType", "001")
//        requestBuilder.addHeader("channel", getChannel())
        requestBuilder.addHeader("androidId", getAndroidID())
    }

    private fun appendChannel(): String {
        val json = JSONObject()
        json.put("deviceId", DeviceUtils.getUniqueDeviceId())
        json.put("channelCode", getChannel())
        json.put("idfa", "")
        json.put("oaid", DeviceIdentifier.getOAID(application))
        json.put("androidId", getAndroidID())
        json.put("imei", getImei())
        json.put("mac", "")
        return json.toString()
    }

    /**
     *  添加checkSum 获取请求参数，拼接后先MD5再Base64
     *  @param request okhttp request
     *  @param requestBuilder okhttp request.builder
     */
    private fun addCheckSum(request: Request, requestBuilder: Request.Builder) {
        val method = request.method
        if ("GET" == method) {
            val paramValues = mutableListOf<String>()
            val httpUrl = request.url
            httpUrl.queryParameterNames.forEach {
                paramValues.add(httpUrl.queryParameter(it)!!)
            }
            val md5 = paramValues.joinToString(separator = "&").encryptMD5()
            val checkSum = Base64.encodeToString(md5.toByteArray(), Base64.NO_WRAP)
            requestBuilder.addHeader("checkSum", checkSum)
        }
        if ("POST" == method) {
            if (request.body is FormBody) {
                val formBody = request.body as FormBody
                val paramValues = mutableListOf<String>()
                for (i in 0 until formBody.size) {
                    paramValues.add(formBody.encodedValue(i))
                }
                val paramsString = paramValues.joinToString(separator = "&")
                val md5 = paramsString.encryptMD5()
                val checkSum = Base64.encodeToString(md5.toByteArray(), Base64.NO_WRAP)
                requestBuilder.addHeader("checkSum", checkSum)
            }
        }
    }

    private fun getImei(): String =
        if (DeviceIdentifier.getIMEI(application) == null) "" else DeviceIdentifier.getIMEI(
            application
        )

    private fun getAndroidID(): String =
        if (DeviceIdentifier.getAndroidID(application) == null) "" else DeviceIdentifier.getAndroidID(
            application
        )

    private fun getChannel(): String{
        val humeChannel = HumeSDK.getChannel(application)
        return if (humeChannel.isNullOrEmpty()) ChannelReaderUtil.getChannel(application.applicationContext).ifNullOrEmpty("djs") else humeChannel
    }
}