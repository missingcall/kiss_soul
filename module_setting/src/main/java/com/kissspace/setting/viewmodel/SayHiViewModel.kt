package com.kissspace.setting.viewmodel

import android.text.Editable
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.SayHiInfoBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.network.result.emitSuccess
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.md516
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class SayHiViewModel : BaseViewModel() {
    val submitEnable = ObservableField(false)
    val textLength = ObservableField("0/50")
    val textString = ObservableField("")

    private val _submitEvent = MutableSharedFlow<ResultState<Boolean>>()
    val submitEvent = _submitEvent.asSharedFlow()

    private val _sayHiTextContent = MutableSharedFlow<ResultState<String>>()
    val sayHiTextContent = _sayHiTextContent.asSharedFlow()

    private val _sayHiPictureInfo = MutableSharedFlow<ResultState<String>>()
    val sayHiPictureInfo = _sayHiPictureInfo.asSharedFlow()

    private val _sayHiAudioInfo = MutableSharedFlow<ResultState<SayHiInfoBean>>()
    val sayHiAudioInfo = _sayHiAudioInfo.asSharedFlow()

    fun onTextChange(editable: Editable) {
        textLength.set("${textString.get()?.length}/50")
        submitEnable.set(textString.get()!!.isNotEmpty())
    }

    fun submitTextInfo() {
        val json = JSONObject()
        json.put("msg", textString.get())
        val param = mutableMapOf<String, Any?>()
        param["messageType"] = "001"
        param["messageContent"] = json.toString()
        request(com.kissspace.setting.http.SettingApi.API_ADD_SAY_HI_INFO, Method.POST, param, state = _submitEvent)
    }

    fun submitAudioInfo(duration: Long, url: String) {
        val json = JSONObject()
        json.put("dur", duration)
        json.put("url", url)
        json.put("ext", "aac")
        json.put("size", 1000)
        json.put("md5", url.md516())
        val param = mutableMapOf<String, Any?>()
        param["messageType"] = "002"
        param["messageContent"] = json.toString()
        request(com.kissspace.setting.http.SettingApi.API_ADD_SAY_HI_INFO, Method.POST, param, state = _submitEvent)
    }

    fun submitPictureInfo(url: String) {
        val json = JSONObject()
        json.put("url", url)
        json.put("ext", "jpg")
        json.put("name", "sayhi")
        json.put("md5", url.md516())
        json.put("w", 1000)
        json.put("h", 1000)
        json.put("size", 1000)
        val param = mutableMapOf<String, Any?>()
        param["messageType"] = "003"
        param["messageContent"] = json.toString()
        request(com.kissspace.setting.http.SettingApi.API_ADD_SAY_HI_INFO, Method.POST, param, state = _submitEvent)
    }

    fun getSayHiTextInfo() {
        request<List<SayHiInfoBean>>(com.kissspace.setting.http.SettingApi.API_REQUEST_SAY_HI_INFO, Method.GET, onSuccess = {
            val textInfo = it.find { that -> that.messageTypeId == "001" }
            if (textInfo != null && (textInfo.examineMessageContent.isNotEmpty() || textInfo.messageContent.isNotEmpty())) {
                val json =
                    if (textInfo.examineMessageContent.isNotEmpty()) JSONObject(textInfo.examineMessageContent) else JSONObject(
                        textInfo.messageContent
                    )
                viewModelScope.launch { _sayHiTextContent.emitSuccess(json.getString("msg")) }
            }
        })
    }

    fun getSayPictureInfo() {
        request<List<SayHiInfoBean>>(com.kissspace.setting.http.SettingApi.API_REQUEST_SAY_HI_INFO, Method.GET, onSuccess = {
            val textInfo = it.find { that -> that.messageTypeId == "003" }
            if (textInfo != null && (textInfo.messageContent.isNotEmpty() || textInfo.examineMessageContent.isNotEmpty())) {
                val json =
                    if (textInfo.examineMessageContent.isNotEmpty()) JSONObject(textInfo.examineMessageContent) else JSONObject(
                        textInfo.messageContent
                    )
                viewModelScope.launch { _sayHiPictureInfo.emitSuccess(json.getString("url")) }
            }
        })
    }

    fun getSayHiAudioInfo() {
        request<List<SayHiInfoBean>>(com.kissspace.setting.http.SettingApi.API_REQUEST_SAY_HI_INFO, Method.GET, onSuccess = {
            val info = it.find { that -> that.messageTypeId == "002" }
            if (info != null) {
                viewModelScope.launch { _sayHiAudioInfo.emitSuccess(info) }
            }
        })
    }


}