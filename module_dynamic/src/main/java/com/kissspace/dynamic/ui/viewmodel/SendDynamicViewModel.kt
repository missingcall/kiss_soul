package com.kissspace.dynamic.ui.viewmodel
import androidx.lifecycle.asLiveData
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/7/31 17:15.
 * @Describe
 */
class SendDynamicViewModel : BaseViewModel() {

    //选择的图片数量
    val selectImageCount = MutableStateFlow(0)

    //发送的动态消息
    val sendDynamicText = MutableStateFlow("")

    //录音时长
    val recordDuration = MutableStateFlow<Long>(0)

    //完成录制
    val finishRecord = MutableStateFlow(false)

    //显示要上传的音频文件
    val showRecordFile = MutableStateFlow(false)

    //是否显示声音
    val isShowVoice = MutableStateFlow(false)

    //是否开启了录音（只要点击了就算开启了）
    val isOpenVoice = MutableStateFlow(false)

    //正在录音
    val isRecording = MutableStateFlow(false)

    //正在播放
    var isPlaying = MutableStateFlow(false)

    var fileImageList = MutableStateFlow<MutableList<String>>(mutableListOf())


    sealed class SelectMode {
        object Picture : SelectMode()
        object Voice : SelectMode()
    }

    var selectedMode = MutableStateFlow<SelectMode>(SelectMode.Picture)

    var isPictureSelected = MutableStateFlow(true)


    val isCouldSend =
        combine(
            isPictureSelected,
            showRecordFile,
            selectImageCount,
            sendDynamicText
        ) { value0, value1, value2, value3 ->
            //只要其中一个条件满足就可以
            !value0 && value1 || value0 && value2 > 0 || value3.isNotEmpty()
        }.asLiveData()

    //是否显示录音文件
    var isShowRecordFile = combine(isPictureSelected, showRecordFile) { value0, value1 ->
        //不选中录音且显示录音文件
        !value0 && value1
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
        speechDuration:Int?=null,
        block: ((Boolean) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["textDynamicContent"] = dynamicText
        param["voiceDynamicContent"] = voiceDynamicContent
        param["speechDuration"] = speechDuration
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

}