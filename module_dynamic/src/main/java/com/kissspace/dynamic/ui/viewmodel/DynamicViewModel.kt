package com.kissspace.dynamic.ui.viewmodel

import androidx.lifecycle.asLiveData
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.model.dynamic.DynamicInfo
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:15.
 * @Describe 动态viewModel
 */
class DynamicViewModel : BaseViewModel() {

    //选择的图片数量
    val selectImageCount = MutableStateFlow(0)

    //发送的动态消息
    val sendDynamicText = MutableStateFlow("")

    //录音时长
    val recordDuration = MutableStateFlow<Long>(0)

    //完成录制
    val finishRecord = MutableStateFlow(false)

    //显示要上传的音频文件
    val isShowRecord = MutableStateFlow(false)

    val isKeyBoardHide = MutableStateFlow(false)

    //正在录音
    val isRecording = MutableStateFlow(false)

    //正在播放
    var isPlaying = MutableStateFlow(false)

    var fileImageList = MutableStateFlow<MutableList<String>>(mutableListOf())


    val isCouldSend =
        combine(isShowRecord, selectImageCount, sendDynamicText) { value1, value2, value3 ->
            //只要其中一个条件满足就可以
            value1 || value2 > 0 || value3.isNotEmpty()
        }.asLiveData()

    val isPlayingOrIsRecording = combine(isRecording, isPlaying) { value1, value2 ->
        value1 || value2
    }.asLiveData()


    val isShowDeleteButton =
        combine(finishRecord, isRecording, isPlaying) { value1, value2, value3 ->
            value1 && !value2 && !value3
        }.asLiveData()


    fun sendDynamic(
        dynamicText: String? = null,
        voiceDynamicContent: String? = null,
        feedbackImageList: List<String>? = null,
        block: ((Boolean) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["textDynamicContent"] = dynamicText
        param["voiceDynamicContent"] = voiceDynamicContent
        val jsonArray = JSONArray()
        feedbackImageList?.map { jsonArray.put(it) }
        param["pictureDynamicContent"] = jsonArray
        request<Boolean>(
            url = DynamicApi.API_INSERT_DYNAMICS,
            method = Method.POST,
            param = param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )

    }

    //分页获取推荐的用户动态列表
    fun getRecommendDynamicList(
        pageNum: Int? = null,
        block: ((DynamicInfo) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = pageNum
        param["pageSize"] = Constants.PageSize
        request<DynamicInfo>(
            url = DynamicApi.API_GET_DYNAMICS_LIST,
            method = Method.GET,
            param = param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )
    }
}