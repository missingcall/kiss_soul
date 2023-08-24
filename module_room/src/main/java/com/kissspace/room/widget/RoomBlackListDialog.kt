package com.kissspace.room.widget

import android.widget.TextView
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.common.model.RoomBlackListUserBean
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogBlacklistBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi

class RoomBlackListDialog(private val crId: String) :
    BaseBottomSheetDialogFragment<RoomDialogBlacklistBinding>() {
    override fun getViewBinding(): RoomDialogBlacklistBinding =
        RoomDialogBlacklistBinding.inflate(layoutInflater)

    override fun initView() {
        mBinding.recyclerView.linear().setup {
            addType<RoomBlackListUserBean>(R.layout.room_dialog_black_list_item)
            onClick(R.id.iv_cancel) {
                val model = getModel<RoomBlackListUserBean>()
                cancelBlackList(model.userId, modelPosition)
            }
        }.mutable = mutableListOf()

        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<List<RoomBlackListUserBean>>(
            RoomApi.API_ROOM_BLACK_LIST,
            Method.GET,
            param,
            onSuccess = {
                if (it.isEmpty()) {
                    mBinding.pageRefreshLayout.showEmpty()
                } else {
                    mBinding.pageRefreshLayout.showContent()
                    mBinding.recyclerView.bindingAdapter.addModels(it)
                }
            })
    }

    private fun cancelBlackList(userId: String, index: Int) {
        val params = mutableMapOf<String, Any?>()
        params["chatRoomId"] = crId
        params["userId"] = userId
        request<Int>(RoomApi.API_CANCEL_BAN_USER_IN_ROOM, Method.POST, params, onSuccess = {
            customToast("已取消拉黑")
            mBinding.recyclerView.mutable.removeAt(index)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(index)
            if (mBinding.recyclerView.mutable.isEmpty()) {
                mBinding.pageRefreshLayout.showEmpty()
            }
        }, onError = {
            customToast(it.message)
        })

    }
}