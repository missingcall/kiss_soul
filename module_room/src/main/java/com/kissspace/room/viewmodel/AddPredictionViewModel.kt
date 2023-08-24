package com.kissspace.room.viewmodel

import androidx.databinding.ObservableField
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.removeSpace
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AddPredictionViewModel : BaseViewModel() {
    val title = ObservableField<String>()
    val leftOption = ObservableField<String>()
    val rightOption = ObservableField<String>()
    val validTime = ObservableField(0L)
    val buttonEnable = ObservableField(false)
    val titleLength = ObservableField(0)
    val leftOptionLength = ObservableField(0)
    val rightOptionLength = ObservableField(0)

    private val _submitEvent = MutableSharedFlow<ResultState<Int>>()
    val submitEvent = _submitEvent.asSharedFlow()

    fun checkInfo() {
        titleLength.set(if (title.get() == null) 0 else title.get()!!.length)
        leftOptionLength.set(if (leftOption.get() == null) 0 else leftOption.get()!!.length)
        rightOptionLength.set(if (rightOption.get() == null) 0 else rightOption.get()!!.length)
        buttonEnable.set(
            title.get()?.isNotEmpty() == true && leftOption.get()
                ?.isNotEmpty() == true && rightOption.get()
                ?.isNotEmpty() == true && validTime.get()!! > 0
        )
    }

    fun submit(crId: String) {
        if (title.get()?.isEmpty() == true) {
            customToast("请输入竞猜标题", true)
            return
        }
        if (leftOption.get()?.isEmpty() == true || rightOption.get()?.isEmpty() == true) {
            customToast("请完善竞猜选项", true)
            return
        }
        if (validTime.get() == 0L) {
            customToast("请选择有效时间", true)
            return
        }
        if (leftOption.get() == rightOption.get()) {
            customToast("选项内容不能重复请重新输入", true)
            return
        }
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["integralGuessTitle"] = title.get()?.removeSpace()
        param["leftOption"] = leftOption.get()?.removeSpace()
        param["rightOption"] = rightOption.get()?.removeSpace()
        param["validTime"] = validTime.get()
        param["creatorId"] = MMKVProvider.userId
        request(RoomApi.API_CREATE_PREDICTION, Method.POST, param, state = _submitEvent)
    }
}