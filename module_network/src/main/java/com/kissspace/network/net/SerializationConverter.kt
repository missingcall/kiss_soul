package com.kissspace.network.net

import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import com.drake.net.request.kType
import com.drake.net.response.file
import com.kissspace.util.isTypeBoolean
import com.kissspace.util.isTypeDouble
import com.kissspace.util.isTypeFloat
import com.kissspace.util.isTypeInt
import com.kissspace.util.isTypeLong
import com.kissspace.util.isTypeString
import com.kissspace.util.jsonDecoder
import com.kissspace.util.logE
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 * Serialization 转换器
 */
class SerializationConverter : NetConverter {
    override fun <R> onConvert(succeed: Type, response: Response): R? {
        val code = response.code
        when {
            code in 200..299 -> { // 请求成功
                if (succeed == File::class.java) {
                    return response.file() as R
                }
                val bodyString = response.body?.string() ?: return null
                val json = JSONObject(bodyString) // 获取JSON中后端定义的错误码和错误信息
                val srvCode = json.getString("responseCode")
                return try {
                    val srvMsg = json.getString("responseMessage")
                    val kType = response.request.kType ?: return null
                    if (srvCode == "200") {
                        when {
                            kType.isTypeString -> json.getString("data") as R
                            kType.isTypeInt -> json.getInt("data") as R
                            kType.isTypeLong -> json.getLong("data") as R
                            kType.isTypeDouble -> json.getDouble("data") as R
                            kType.isTypeBoolean -> json.getBoolean("data") as R
                            else -> if (json.getString("data") == "null") {
                                null
                            } else {
                                json.getString("data").parseBody<R>(kType)
                            }
                        }

                    } else {
                        throw ResponseException(response, srvMsg, tag = srvCode)
                    }
                } catch (e: SerializationException) { //解析失败 抛出异常
                    throw ConvertException(response, e.message, tag = srvCode)
                }
            }

            code in 400..499 -> throw RequestParamsException(
                response,
                tag = code
            ) // 请求参数错误
            code >= 500 -> throw ServerResponseException(
                response = response,
                tag = code
            ) // 服务器异常错误
            else -> throw ConvertException(response)
        }
    }

    private fun <R> String.parseBody(succeed: KType): R? {
        return jsonDecoder.decodeFromString(
            jsonDecoder.serializersModule.serializer(succeed),
            this
        ) as R
    }
}

