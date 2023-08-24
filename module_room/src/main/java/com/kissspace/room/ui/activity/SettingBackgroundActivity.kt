package com.kissspace.room.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.model.RoomBackgroundListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.customToast
import com.kissspace.util.resToColor
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomActivitySettingBackgroundBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.util.immersiveStatusBar

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/22 16:07
 * @Description: 设置房间背景页面
 *
 */
@Router(path = RouterPath.PATH_ROOM_SETTING_BACKGROUND)
class SettingBackgroundActivity : com.kissspace.common.base.BaseActivity(R.layout.room_activity_setting_background) {
    private val currentPath: String by parseIntent()
    private val crId by parseIntent<String>()
    private val mBinding by viewBinding<RoomActivitySettingBackgroundBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        mBinding.titleBar.setMarginStatusBar()
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }
        })
        mBinding.tvSubmit.setOnClickListener {
            val adapter = mBinding.recyclerView.bindingAdapter
            if (adapter.checkedPosition.isEmpty()) {
                customToast("请选择背景图")
            } else {
                val model = adapter.getCheckedModels<RoomBackgroundListBean>()[0]
                submitBackground(model)
            }
        }

        val param = mutableMapOf<String, Any?>("roomId" to crId)
        request<MutableList<RoomBackgroundListBean>>(
            RoomApi.API_QUERY_ROOM_BACKGROUND,
            Method.GET,
            param,
            onSuccess = {
                initRecyclerView(it)
            })
    }

    private fun submitBackground(model:RoomBackgroundListBean) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["dynamicImage"] = model.dynamicImage
        param["staticImage"] = model.staticImage
        request<Int>(RoomApi.API_SETTING_ROOM_BACKGROUND, Method.POST, param, onSuccess = {
            finish()
        }, onError = {
            ToastUtils.showLong(it.message)
        })
    }

    private fun initRecyclerView(list: List<RoomBackgroundListBean>) {
        mBinding.recyclerView.grid(3)
            .divider(R.drawable.room_divider_select_background, DividerOrientation.GRID).setup {
                addType<RoomBackgroundListBean> { R.layout.room_layout_background_picture_item }
                singleMode = true
                onChecked { position, isChecked, _ ->
                    val model = getModel<RoomBackgroundListBean>(position)
                    model.hadChosen = isChecked
                    model.notifyChange()
                    if (model.hadChosen) {
                        mBinding.tvSubmit.setBackgroundResource(R.drawable.room_bg_select_background_button_checked)
                        mBinding.tvSubmit.setTextColor(com.kissspace.module_common.R.color.common_white.resToColor())
                    } else {
                        mBinding.tvSubmit.setBackgroundResource(R.drawable.room_bg_select_background_button_normal)
                        mBinding.tvSubmit.setTextColor(com.kissspace.module_common.R.color.color_505050.resToColor())
                    }
                }
                onFastClick(R.id.root) {
                    checkedSwitch(modelPosition)
                }
            }.models = list
        mBinding.recyclerView.bindingAdapter.setChecked(list.indexOfFirst { it.hadChosen }, true)
    }
}

