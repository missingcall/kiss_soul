package com.kissspace.mine.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.feedback.FeedBackModel
import com.kissspace.common.model.feedback.FeedBackRecodeModel
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/1/7 10:03.
 * @Describe
 */
class FeedBackViewModel : BaseViewModel() {

    val feedBackText = MutableLiveData<String>()

    val isShowEmpty= MutableLiveData<Boolean>()

    val selectImageCount = MutableLiveData<Int>()

    val isShowRedSpot = MutableLiveData<Boolean>()

    val selectMaxImageCount = 3

    val isUploadEnable = MediatorLiveData<Boolean>().apply {
        addSource(feedBackText) {
            setValue(it.isNotEmpty())
        }
    }

    val feedBackTextType = ObservableField<String>()

    //获取反馈类型列表
    fun getFeedBackListType(block: ((MutableList<FeedBackModel>) -> Unit)?) {
        request<MutableList<FeedBackModel>>(
            url = MineApi.API_QUERY_USER_FEEDBACK_TYPE,
            method = Method.GET,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )
    }

    //添加反馈
    fun addFeedBackType(
        feedbackTypeId: String,
        feedbackDescribe: String,
        feedbackImageList: List<String>,
        block: ((Int) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["feedbackTypeId"] = feedbackTypeId
        param["feedbackDescribe"] = feedbackDescribe
        val jsonArray = JSONArray()
        feedbackImageList.map { jsonArray.put(it) }
        param["feedbackImage"] = jsonArray
        request<Int>(
            url = MineApi.API_INSERT_USER_FEEDBACK,
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

    //获取反馈类型记录
    fun getFeedBackListRecode(block: ((MutableList<FeedBackRecodeModel>) -> Unit)?) {
        request<MutableList<FeedBackRecodeModel>>(
            url = MineApi.API_QUERY_USER_FEEDBACK,
            method = Method.GET,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )
    }

    //已读信息之后修改成已读状态
    fun readUserFeedback(feedbackId:String,block: ((Boolean) -> Unit)?={}) {
        val param = mutableMapOf<String, Any?>("feedbackId" to feedbackId)
        request<Boolean>(
            url = MineApi.API_READ_STATUS_USER_FEEDBACK,
            method = Method.GET,
            param= param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )
    }

}