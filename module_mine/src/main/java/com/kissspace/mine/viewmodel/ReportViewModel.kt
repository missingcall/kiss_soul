package com.kissspace.mine.viewmodel

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.ReportTypeListModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.isNotEmptyBlank
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/1/6 19:25.
 * @Describe
 */
class ReportViewModel : BaseViewModel() {


    val reportText = MutableLiveData<String>()

    val reportType = MutableLiveData<String>()

    val selectImageCount = MutableLiveData<Int>()

    val selectMaxImageCount = 9

    val isUploadEnable = DJSLiveData<Boolean>().apply {
        addSources(reportText,reportType) {
            setValue(reportText.value.isNotEmptyBlank()&&reportType.value.isNotEmptyBlank())
        }
    }

    fun queryReportInformantType(block: ((List<ReportTypeListModel>?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        request<List<ReportTypeListModel>?>(
            MineApi.API_QueryInformantType,
            Method.GET,
            param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )
    }

    //举报用户
    fun reportUser(reportTypeId: String,
                           feedbackDescribe: String,
                           userId:String,
                           feedbackImageList: List<String>,
                           block: ((Boolean) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        param["informantTypeId"] = reportTypeId
        param["informantReason"] = feedbackDescribe
        param["informedPersonId"] = userId
        val jsonArray = JSONArray()
        feedbackImageList.map { jsonArray.put(it) }
        param["informantImage"] = jsonArray
        request<Boolean>(
            url = MineApi.API_REPORT_USER,
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
    //举报房间
    fun reportLiveRoom(
        roomId:String,
        roomHeaderId:String,
        reportTypeId: String,
        feedbackDescribe: String,
        feedbackImageList: List<String>,
        block: ((Boolean) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["informantRoomId"] = roomId
        param["informantLandlordId"] = roomHeaderId
        param["informantTypeId"] = reportTypeId
        param["informantReason"] = feedbackDescribe
        val jsonArray = JSONArray()
        feedbackImageList.map { jsonArray.put(it) }
        param["informantImage"] = jsonArray
        request<Boolean>(
            url = MineApi.API_REPORT_CHATROOM,
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