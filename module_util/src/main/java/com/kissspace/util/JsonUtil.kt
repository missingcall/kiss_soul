package com.kissspace.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

val jsonDecoder: Json
    get() = Json {
        isLenient = true // 允许非双引号包裹键值
        ignoreUnknownKeys = true  //允许反序列化的数据类缺失字段
        coerceInputValues = true // 如果JSON字段是Null则使用默认值
        allowStructuredMapKeys = true //启用map序列化
    }

inline fun <reified T> toJson(value: T): String =
    jsonDecoder.encodeToString(jsonDecoder.serializersModule.serializer(), value)


inline fun <reified T> fromJson(jsonString: String): T = jsonDecoder.decodeFromString(jsonString)